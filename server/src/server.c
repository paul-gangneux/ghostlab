#include "server.h"

#define send_and_check_error(cli_fd, ansbuf, len)\
  if(!send_msg(cli_fd, ansbuf, len)) goto end

#define send_str_and_check_error(cli_fd, str)\
  if(!send_string(cli_fd, str)) goto end

#define write_stdout_and_check_error(buf, len)\
  if (write(STDOUT_FILENO, buf, len) < 0) perror("write")

// checks that tcp message has the
// proper size and ends with ***
#define check_tcp_message(buf, n)\
  n >= 8 &&\
  buf.req[n-1] == '*' &&\
  buf.req[n-2] == '*' &&\
  buf.req[n-3] == '*'

// expecting [NEWPL username port***]
#define update_player_infos(player, reqbuf)\
  memmove(player->name, reqbuf.req + 6, MAX_NAME);\
  char portstr[5];\
  portstr[4] = 0;\
  memmove(portstr, reqbuf.req + 15, 4);\
  int port = atoi(portstr);\
  player->addr.sin_port = htons(port)

#define comp_keyword(reqbuf, word)\
  reqbuf.req[0] == word[0] &&\
  reqbuf.req[1] == word[1] &&\
  reqbuf.req[2] == word[2] &&\
  reqbuf.req[3] == word[3] &&\
  reqbuf.req[4] == word[4]

#define get_move_amount(reqbuf)\
  (reqbuf.req[6] - '0') * 100 +\
  (reqbuf.req[7] - '0') * 10 +\
  (reqbuf.req[8] - '0')

int verbose;
int very_verbose;
int print_mazes;
int easy_mazes;
int ghost_delay;
int not_inverse_xy;
const char* multicast_ip_address;

gameList_t* gameList;

void print_help(const char* progName) {
  printf(
    "Usage: %s [-vVhmse] [-p port] [-t delay]\n\n"
    "options\n"
    "    -p port\n"
    "        Sets listening port to option argument.\n"
    "        Default port is 4242.\n\n"
    "    -v\n"
    "        Verbose mode, see client connections / disconnections\n"
    "        and game creations / destructions.\n\n"
    "    -V\n"
    "        Very verbose mode. Prints all incoming and outcoming\n"
    "        network messages.\n\n"
    "    -i\n"
    "        Does not inverse X and Y pos when sending / receiving messages\n\n"
    "    -m\n"
    "        Prints generated mazes.\n\n"
    "    -s\n"
    "        Fixes seed for random calculations at 0.\n\n"
    "    -e\n"
    "        Easy maze generation. Useful for tests.\n\n"
    "    -t delay\n"
    "        Time it takes (in seconds) for ghosts to change position.\n"
    "        Default is 5, minimum is 1, maximum is 60.\n\n"
    "    -h\n"
    "        Displays help.\n"
    , progName);
}

int main(int argc, char* argv[]) {

  multicast_ip_address = "225.100.100.100";

  int accept_port = 4242;
  verbose = 0;
  very_verbose = 0;
  print_mazes = 0;
  easy_mazes = 0;
  ghost_delay = 5;
  not_inverse_xy = 0;

  srandom(time(0));

  int opt;
  while ((opt = getopt(argc, argv, "Vvhsiemt:p:")) != -1) {
    switch (opt) {
      case 'v':
        verbose = 1;
        break;
      case 'V':
        very_verbose = 1;
        break;
      case 'm':
        print_mazes = 1;
        break;
      case 'i':
        not_inverse_xy = 1;
        break;
      case 'p':
        accept_port = atoi(optarg);
        if (accept_port == 0) {
          print_help(argv[0]);
          return -1;
        }
        break;
      case 's':
        srandom(0);
        break;
      case 'e':
        easy_mazes = 1;
        break;
      case 't':
        ghost_delay = atoi(optarg);
        break;
      case 'h':
        print_help(argv[0]);
        return 0;
      default:
        print_help(argv[0]);
        return -1;
    }
  }

  int sock = init_server_socket(accept_port);
  if (listen(sock, 0) == -1) {
    perror("listen error");
    exit(1);
  }

  init_udp_sock();

  printf("server created on port %d\n", accept_port);
  gameList = newGameList();
  socklen_t socklen = sizeof(struct sockaddr_in);

  while (1) {
    struct sockaddr_in caller;
    int cli_fd = accept(sock, (struct sockaddr*) &caller, &socklen);
    if (cli_fd == -1) {
      // no need to stop the server
      perror("accept error");
    }
    else {
      // memory is allocated here
      player_t* player = newPlayer(cli_fd, caller);

      pthread_t thread;
      pthread_create(&thread, NULL, interact_with_client, (void*) player);
    }
  }
}

// returns fd or newly created TCP socket
int init_server_socket(int port) {
  int sock = socket(AF_INET, SOCK_STREAM, 0);
  if (sock == -1) {
    perror("socket error");
    exit(1);
  }

  struct sockaddr_in addr = {
    .sin_family = AF_INET,
    .sin_port = htons(port),
    .sin_addr = {
      .s_addr = htons(INADDR_ANY),
    }
  };

  int r = bind(sock, (struct sockaddr*) &addr, sizeof(struct sockaddr_in));
  if (r == -1) {
    perror("bind error");
    exit(1);
  }

  return sock;
}

#define charIsAlphaNum(c)\
  (\
    (c >= 'a' && c <= 'z') ||\
    (c >= 'A' && c <= 'Z') ||\
    (c >= '0' && c <= '9')\
  )\

int username_isValid(char username[MAX_NAME]) {
  int i = MAX_NAME - 1;
  while (i >= 0 && username[i] == '\0') {
    i--;
  }
  if (i == -1) // empty username
    return 0;

  while (i >= 0) {
    if (!charIsAlphaNum(username[i]))
      return 0;
    i--;
  }
  return 1;
}

// very big function
void* interact_with_client(void* arg) {
  int isInGame = 0;
  player_t* player = (player_t*) arg;
  game_t* game = NULL;
  int cli_fd = player->fd;
  int n = 0, len = 0;
  int exit_loop = 0;

  reqbuf_t reqbuf;
  rw_buffers_initialize(&reqbuf);

  char ansbuf[64];

  if (verbose)
    printf("new client connected\n");

  // sends GAMES and OGAME to client
  n = gameList_sendToCli(gameList, cli_fd);
  if (n == -1) {
    goto end;
  }

  // FIRST LOOP - BEFORE GAME
  // client creates or joins game
  while (!exit_loop) {
    len = next_request(player, &reqbuf);
    if (len <= 0) {
      switch (len) {
        case REQ_ERROR:
          perror("next_request");
          goto end;
        case REQ_DISCONNECTION:
          if (verbose)
            printf("client disconnected\n");
          goto end;
        case REQ_FORCE_END:
          if (verbose)
            printf("client disconnected by server\n");
          goto end;
        default:
          printf("unknown next_request error, disconnecting client\n");
          goto end;
      }
    }

    if (!(check_tcp_message(reqbuf, len)) && verbose) {
      printf("bad client request, discarding\n");
    }
    // new game creation
    // expecting [NEWPL username port***]
    else if (comp_keyword(reqbuf, "NEWPL") && len == 22) {
      char username[MAX_NAME];
      memmove(username, reqbuf.req + 6, MAX_NAME);
      if (isInGame) {
        send_str_and_check_error(cli_fd, "DUNNO***");
      }
      else if (!username_isValid(username)) {
        send_str_and_check_error(cli_fd, "REGNO***");
      }
      else {
        update_player_infos(player, reqbuf);
        game = newGame();
        int id = gameList_add(gameList, game);
        if (id == -1) {
          // shouldn't happen, ever
          printf("error at addToGameList");
          goto end;
        }

        n = game_addPlayer(gameList, id, player);
        if (n == -1) {
          gameList_remove(gameList, game, RM_FORCE);
          game = NULL;
          send_str_and_check_error(cli_fd, "REGNO***");
        }
        else {
          isInGame = 1;
          memmove(ansbuf, "REGOK 0***", 10);
          ansbuf[6] = (u_int8_t) id;
          send_and_check_error(cli_fd, ansbuf, 10);

          if (verbose)
            printf("new game created with id %d\n", id);
        }
      }
    }
    // joining game
    // expecting [REGIS username port m***]
    else if (comp_keyword(reqbuf, "REGIS") && len == 24) {
      char username[MAX_NAME];
      memmove(username, reqbuf.req + 6, MAX_NAME);
      if (isInGame) {
        send_str_and_check_error(cli_fd, "DUNNO***");
      }
      else if (!username_isValid(username)) {
        send_str_and_check_error(cli_fd, "REGNO***");
      }
      else {
        update_player_infos(player, reqbuf);
        u_int8_t id = reqbuf.req[20];

        game = game_get(gameList, id);
        if (game == NULL) {
          send_str_and_check_error(cli_fd, "REGNO***");
        }

        else {
          n = game_addPlayer(gameList, id, player);
          if (n == -1) {
            send_str_and_check_error(cli_fd, "REGNO***");
            game = NULL;
          }
          else {
            isInGame = 1;
            memmove(ansbuf, "REGOK 0***", 10);
            ansbuf[6] = (u_int8_t) id;
            send_and_check_error(cli_fd, ansbuf, 10);
          }
        }
      }
    }
    // leaves game
    // expecting [UNREG***]
    else if (comp_keyword(reqbuf, "UNREG") && len == 8) {
      if (!isInGame) {
        send_str_and_check_error(cli_fd, "DUNNO***");
      }
      else {
        u_int8_t id_game = game->id;
        game_removePlayer(game, player, NO_END_THREAD);
        // game removed if no players are left in it
        int removed = gameList_remove(gameList, game, RM_NOPLAYERS);
        if (!removed)
          game_startIfAllReady(game);
        isInGame = 0;
        memmove(ansbuf, "UNROK 0***", 10);
        ansbuf[6] = id_game;
        send_and_check_error(cli_fd, ansbuf, 10);
      }
    }
    // ready to start game
    // expecting [START***]
    else if (comp_keyword(reqbuf, "START") && len == 8) {
      if (!isInGame) {
        send_str_and_check_error(cli_fd, "DUNNO***");
      }
      else {
        exit_loop = 1;
      }
    }
    // asking for game length and width
    // expecting [SIZE? m***]
    else if (comp_keyword(reqbuf, "SIZE?") && len == 10) {
      u_int8_t id_game = reqbuf.req[6];
      u_int16_t h, w;
      game_t* temp = game_getSize(gameList, id_game, &h, &w);
      if (temp == NULL) {
        send_str_and_check_error(cli_fd, "DUNNO***");
      }
      else {
        h = htole16(h);
        w = htole16(w);

        memmove(ansbuf, "SIZE! m hh ww***", 16);
        memmove(ansbuf + 6, &id_game, sizeof(id_game));
        memmove(ansbuf + 8, &h, sizeof(h));
        memmove(ansbuf + 11, &w, sizeof(w));
        send_and_check_error(cli_fd, ansbuf, 16);
      }
    }
    // asking for player list
    // expecting [LIST? m***]
    else if (comp_keyword(reqbuf, "LIST?") && len == 10) {
      u_int8_t id_game = reqbuf.req[6];
      n = game_sendPlayerList(gameList, id_game, cli_fd);
    }
    // asking for game list
    // expecting [GAME?***]
    else if (comp_keyword(reqbuf, "GAME?") && len == 8) {
      // sends GAMES and OGAME to client
      n = gameList_sendToCli(gameList, cli_fd);
      if (n == -1) {
        goto end;
      }
    }
    // wrong input
    else if (verbose) {
      // send_str_and_check_error(cli_fd, "DUNNO***");
      printf("unknown client request, discarding\n");
    }
  }

  player->is_ready = 1;
  game_randomizePosition(game, player);
  game_startIfAllReady(game);
  exit_loop = 0;

  // SECOND LOOP - WAITING FOR ALL PLAYERS
  while (!exit_loop) {
    // game_startIfAllReady() écrira dans le pipe du player quand le jeu pourra commencer
    len = next_request(player, &reqbuf);

    if (len <= 0) {
      switch (len) {
        case REQ_ERROR:
          perror("next_request");
          goto end;
        case REQ_DISCONNECTION:
          if (verbose)
            printf("client disconnected\n");
          goto end;
        case REQ_FORCE_END:
          if (verbose)
            printf("client disconnected by server\n");
          goto end;
        case REQ_GAME_START:
          exit_loop = 1;
          break;
        default:
          printf("unknown next_request error, disconnecting client\n");
          goto end;
      }
    }

    else if (verbose) {
      printf("waiting for game to start, ignoring request\n");
    }
  }

  send_welcome_msg(player, game, ansbuf);

  int direction;
  int amount;
  exit_loop = 0;

  // THIRD LOOP - IN GAME
  while (!exit_loop) {
    direction = MV_NONE;
    amount = 0;
    len = next_request(player, &reqbuf);

    if (len <= 0) {
      switch (len) {
        case REQ_ERROR:
          perror("next_request");
          goto end;
        case REQ_DISCONNECTION:
          if (verbose)
            printf("client disconnected\n");
          goto end;
        case REQ_FORCE_END:
          if (verbose)
            printf("client disconnected by server\n");
          goto end;
        case REQ_GAME_END:
          exit_loop = 1;
          break;
        default:
          printf("unknown next_request error, disconnecting client\n");
          goto end;
      }
    }

    else if (!(check_tcp_message(reqbuf, len))) {
      printf("bad client request, discarding\n");
    }
    // expecting [UPMOV ddd***]
    else if (comp_keyword(reqbuf, "UPMOV") && len == 12) {
      direction = MV_UP;
    }
    // expecting [DOMOV ddd***]
    else if (comp_keyword(reqbuf, "DOMOV") && len == 12) {
      direction = MV_DOWN;
    }
    // expecting [LEMOV ddd***]
    else if (comp_keyword(reqbuf, "LEMOV") && len == 12) {
      direction = MV_LEFT;
    }
    // expecting [RIMOV ddd***]
    else if (comp_keyword(reqbuf, "RIMOV") && len == 12) {
      direction = MV_RIGHT;
    }
    // expecting [LIGH?***]
    else if (comp_keyword(reqbuf, "LIGH?") && len == 8) {
      memmove(ansbuf, "LIGHT xxxxxxxx***", 17);
      game_getSurroundings(game, player, ansbuf + 6);
      send_msg(cli_fd, ansbuf, 17);
    }
    // expecting [GLIS?***]
    else if (comp_keyword(reqbuf, "GLIS?") && len == 8) {
      // sends [GLIS! s***] and [GPLYR username xxx yyy pppp***]
      if (!game_sendPlayerList_AllInfos(game, cli_fd))
        goto end;
    }
    // expecting [MALL? mess***]
    else if (comp_keyword(reqbuf, "MALL?") && len >= 10) {
      int newlen = len + 9;
      char* messbuf = (char*) malloc(newlen);

      memmove(messbuf, "MESSA ", 6);
      memmove(messbuf + 6, player->name, MAX_NAME);
      memmove(messbuf + 14, reqbuf.req + 5, len - 5);

      messbuf[newlen - 1] = '+';
      messbuf[newlen - 2] = '+';
      messbuf[newlen - 3] = '+';

      // sends [MESSA username mess+++] to all players
      send_msg_multicast(game, messbuf, newlen);
      free(messbuf);
      send_str_and_check_error(cli_fd, "MALL!***");
    }
    // expecting [SEND? username mess***]
    else if (comp_keyword(reqbuf, "SEND?") && len >= 19) {
      char destId[MAX_NAME];
      memmove(destId, reqbuf.req + 6, MAX_NAME);

      // we use reqbuf.req as a send buffer. kinda dirty but works
      memmove(reqbuf.req, "MESSP", 5);
      memmove(reqbuf.req + 6, player->name, MAX_NAME);
      reqbuf.req[len - 1] = '+';
      reqbuf.req[len - 2] = '+';
      reqbuf.req[len - 3] = '+';

      // sends [MESSP username mess+++] to dest player
      if (game_sendMessageToOnePlayer(game, destId, reqbuf.req, len)) {
        send_str_and_check_error(cli_fd, "SEND!***");
      }
      else {
        send_str_and_check_error(cli_fd, "NSEND***");
      }
    }
    // expecting [IQUIT***]
    else if (comp_keyword(reqbuf, "IQUIT") && len == 8) {
      send_str_and_check_error(cli_fd, "GOBYE***");
      goto end;
    }
    else if (verbose) {
      printf("unknown client request, discarding\n");
    }

    if (direction != MV_NONE) {
      amount = get_move_amount(reqbuf);
      n = game_movePlayer(game, player, amount, direction);
      int size;
      if (n == 0) {
        memmove(ansbuf, "MOVE! xxx yyy***", 16);
        size = 16;
      }
      else {
        memmove(ansbuf, "MOVEF xxx yyy pppp***", 21);
        size = 21;
        mv_num4toBuf(ansbuf, 14, player->score);
      }
      if(not_inverse_xy) {
        mv_num3toBuf(ansbuf, 6, player->x);
        mv_num3toBuf(ansbuf, 10, player->y);
      } else {
        mv_num3toBuf(ansbuf, 6, player->y);
        mv_num3toBuf(ansbuf, 10, player->x);
      }
      

      // sends [MOVE! x y***] or [MOVEF x y p***]
      send_and_check_error(cli_fd, ansbuf, size);
      if (n) {
        game_endIfNoGhost(game);
      }
    }
  }

  exit_loop = 0;

  // FOURTH LOOP - AFTER GAME ENDED
  while (!exit_loop) {
    len = next_request(player, &reqbuf);

    if (len <= 0) {
      switch (len) {
        case REQ_ERROR:
          perror("next_request");
          goto end;
        case REQ_DISCONNECTION:
          if (verbose)
            printf("client disconnected\n");
          goto end;
        case REQ_FORCE_END:
          if (verbose)
            printf("client disconnected by server\n");
          goto end;
        default:
          printf("unknown next_request error, disconnecting client\n");
          goto end;
      }
    }

    else if (!(check_tcp_message(reqbuf, len))) {
      printf("bad client request, discarding\n");
    }
    else if (comp_keyword(reqbuf, "LIGH?") && len == 8) {
      // do nothing
    }

    // expecting anything
    else {
      send_str_and_check_error(cli_fd, "GOBYE***");
      goto end;
    }
  }

  end:

  if (isInGame) {
    game_removePlayer(game, player, END_THREAD);
    // game removed if no players are left in it
    gameList_remove(gameList, game, RM_NOPLAYERS);
  }
  freePlayer(player);
  return NULL;
}