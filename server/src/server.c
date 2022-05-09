#include "server.h"

#define MIN(a, b) (a<b?a:b)
#define MAX(a, b) (a>b?a:b)

#define check_error(n)\
  if (n < 0) {\
    perror("send");\
    goto end;\
  }

#define send_string(sock, str) send_msg(sock, str, strlen(str))

#define print_incoming_req(reqbuf, len)\
  n = write(STDOUT_FILENO, "<- ", 3);\
  check_error(n);\
  n = write(STDOUT_FILENO, reqbuf.req, len);\
  check_error(n);\
  n = write(STDOUT_FILENO, "\n", 1);\
  check_error(n)

// checks that tcp message has the
// proper size and ends with ***
#define check_tcp_message(buf, n)\
  n >= 8 &&\
  buf.req[n-1] == '*' &&\
  buf.req[n-2] == '*' &&\
  buf.req[n-3] == '*'

// TODO: check is username is alphanum
// expecting [NEWPL username port***]
#define update_player_infos(player, reqbuf)\
  memmove(player->name, reqbuf.req + 6, MAX_NAME);\
  char portstr[5];\
  portstr[4] = 0;\
  memmove(portstr, reqbuf.req + 15, 4);\
  int port = atoi(portstr);\
  player->addr.sin_port = htons(port)

int verbose;
int very_verbose;
const char* multicast_address;

gameList_t* gameList;

void print_help(const char* progName) {
  printf(
    "Usage: %s [-vVh] -p port\n\n"
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
    "    -h\n"
    "        Displays help.\n"
    , progName);
}

int main(int argc, char* argv[]) {

  multicast_address = "225.100.100.100";

  int accept_port = 4242;
  verbose = 0;
  very_verbose = 0;

  int opt;
  while ((opt = getopt(argc, argv, "Vvhp:")) != -1) {
    switch (opt) {
      case 'v':
        verbose = 1;
        break;
      case 'V':
        very_verbose = 1;
        break;
      case 'p':
        accept_port = atoi(optarg);
        if (accept_port == 0) {
          print_help(argv[0]);
          return -1;
        }
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

  printf("server created on port %d\n", accept_port);

  gameList = newGameList();

  // as a test
  // game_t* test_game = newGame();
  // gameList_add(gameList, test_game);
  // game_t* test_game2 = newGame();
  // gameList_add(gameList, test_game2);

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

// renvoie le FD du socket TCP créé
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

#define READBUF_SIZE 1024
#define REQ_SIZE 512

typedef struct reqbuf {
  char readbuf[READBUF_SIZE];
  char req[REQ_SIZE];
  int beg, end; // cursors
} reqbuf_t;

void rw_buffers_initialize(reqbuf_t* buf) {
  memset(buf->readbuf, 0, READBUF_SIZE);
  memset(buf->req, 0, REQ_SIZE);
  buf->beg = 0;
  buf->end = 0;
}

// puts nect tcp request in buf->reqbuf
// returns length of request if sucessful,
// -1 on error (client disconnexion)
int nextRequest(player_t* player, reqbuf_t* reqbuf) {
  //TODO: bien tester tout ça
  int n = 0;
  if (reqbuf->beg >= READBUF_SIZE - REQ_SIZE) {
    memmove(reqbuf->readbuf, reqbuf->readbuf + reqbuf->beg, reqbuf->end - reqbuf->beg);
    reqbuf->end -= reqbuf->beg;
    reqbuf->beg = 0;
  }

  if (reqbuf->beg == reqbuf->end) {
    reqbuf->beg = 0;
    reqbuf->end = 0;

    struct pollfd pollfd[2] = {
      {.fd = player->fd, .events = POLLIN},
      {.fd = player->pipe[0], .events = POLLIN}
    };
    poll(pollfd, 2, -1);

    if (pollfd[1].revents & POLLIN) {
      char buf[3] = { 0, 0, 0 };
      n = read(player->pipe[0], buf, 3);
      if (n == 3 && buf[0] == 'e' && buf[1] == 'n' && buf[2] == 'd')
        return -2; // force end
      if (n == 3 && buf[0] == 'b' && buf[1] == 'g' && buf[2] == 'n')
        return -3; // game begins
    }

    n = recv(player->fd, reqbuf->readbuf, READBUF_SIZE, 0);
    if (n == -1)
      return -1;
    reqbuf->end += n;
    if (n <= 3) {
      // discarding
      reqbuf->beg = reqbuf->end;
      return n;
    }
  }

  char* tempbuf = reqbuf->readbuf + reqbuf->beg;
  n = 1;
  while (reqbuf->beg + n < reqbuf->end) {
    if (tempbuf[n - 1] == '*' && tempbuf[n - 2] == '*' && tempbuf[n - 3] == '*') {
      if (n < reqbuf->end && tempbuf[n] == '*') n++; // au cas ou un byte = 42
      break;
    }
    n++;
  }

  // n = MIN(n, REQ_SIZE); // just to be safe // test this // nah

  if (n > 0)
    memmove(reqbuf->req, tempbuf, n);

  if (n >= REQ_SIZE) {
    printf("request too long, truncated\n");
    reqbuf->req[REQ_SIZE - 1] = '*';
    reqbuf->req[REQ_SIZE - 2] = '*';
    reqbuf->req[REQ_SIZE - 3] = '*';
  }

  reqbuf->beg += n;
  return n;
}

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

#define nb_to_char(nb, factor) ((nb / factor) % 10) + '0'

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
    len = nextRequest(player, &reqbuf);
    if (len == -1) {
      perror("nextRequest");
      goto end;
    }
    if (len == 0) {
      if (verbose)
        printf("client disconnected\n");
      goto end;
    }
    if (len == -2) {
      if (verbose)
        printf("client disconnected by server\n");
      goto end;
    }
    if (very_verbose && len > 0) {
      print_incoming_req(reqbuf, len);
    }
    if (!(check_tcp_message(reqbuf, len))) {
      printf("bad client request, discarding\n");
    }
    // new game creation
    // expecting [NEWPL username port***]
    else if (comp_keyword(reqbuf, "NEWPL") && len == 22) {
      if (isInGame) {
        send_string(cli_fd, "DUNNO***");
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

        memmove(ansbuf, "REGOK 0***", 10);
        ansbuf[6] = (u_int8_t) id;
        send_msg(cli_fd, ansbuf, 10);

        game_addPlayer(gameList, id, player);
        if (verbose)
          printf("new game created with id %d\n", id);
        isInGame = 1;
      }

    }
    // joining game
    // expecting [REGIS username port m***]
    else if (comp_keyword(reqbuf, "REGIS") && len == 24) {

      update_player_infos(player, reqbuf);
      u_int8_t id = reqbuf.req[20];

      game = game_get(gameList, id);
      if (game == NULL) {
        send_string(cli_fd, "DUNNO***");
      }

      else {
        n = game_addPlayer(gameList, id, player);
        if (n == -1) {
          send_string(cli_fd, "DUNNO***");
        }
        else {
          // redundant code sue me
          memmove(ansbuf, "REGOK 0***", 10);
          ansbuf[6] = (u_int8_t) id;
          send_msg(cli_fd, ansbuf, 10);
          isInGame = 1;
        }
      }
    }
    // leaves game
    // expecting [UNREG***]
    else if (comp_keyword(reqbuf, "UNREG") && len == 8) {
      if (!isInGame) {
        send_string(cli_fd, "DUNNO***");
      }
      else {
        u_int8_t id_game = game->id;
        game_removePlayer(game, player);
        // game removed if no players are left in it
        gameList_remove(gameList, game, RM_NOPLAYERS);
        isInGame = 0;
        memmove(ansbuf, "UNROK 0***", 10);
        ansbuf[6] = id_game;
        send_msg(cli_fd, ansbuf, 10);
      }
    }
    // ready to start game
    // expecting [START***]
    else if (comp_keyword(reqbuf, "START") && len == 8) {
      if (!isInGame) {
        send_string(cli_fd, "DUNNO***");
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
        send_string(cli_fd, "DUNNO***");
      }
      else {
        h = htole16(h);
        w = htole16(w);

        memmove(ansbuf, "SIZE! m hh ww***", 16);
        memmove(ansbuf + 6, &id_game, sizeof(id_game));
        memmove(ansbuf + 8, &h, sizeof(h));
        memmove(ansbuf + 11, &w, sizeof(w));
        send_msg(cli_fd, ansbuf, 16);
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
    else {
      // send_string(cli_fd, "DUNNO***");
      printf("unknown client request, discarding\n");
    }
  }

  player->is_ready = 1;
  game_randomizePosition(game, player);
  game_startIfAllReady(game);

  exit_loop = 0;
  int direction;
  int amount;

  // SECOND LOOP - IN GAME
  while (!exit_loop) {
    direction = MV_NONE;
    amount = 0;
    len = nextRequest(player, &reqbuf);
    if (len == -1) {
      perror("nextRequest");
      goto end;
    }
    if (len == 0) {
      printf("client disconnected\n");
      goto end;
    }
    if (len == -2) {
      printf("client disconnected by server\n");
      goto end;
    }
    if (very_verbose && len > 0) {
      print_incoming_req(reqbuf, len);
    }
    if (len == -3) { // game begins
      memmove(ansbuf, "WELCO m hh ww f ip_.___.___.___ port***", 39);
      ansbuf[6] = game->id;
      u_int16_t h = htole16(game->h);
      u_int16_t w = htole16(game->w);
      memmove(ansbuf + 8, &h, sizeof(u_int16_t));
      memmove(ansbuf + 11, &w, sizeof(u_int16_t));
      ansbuf[14] = game->nb_ghosts;
      memmove(ansbuf + 16, multicast_address, 15);
      memmove(ansbuf + 32, game->multicast_port, 4);

      // sending [WELCO m h w f ip port***]
      send_msg(cli_fd, ansbuf, 39);

      memmove(ansbuf, "POSIT username xxx yyy***", 25);
      memmove(ansbuf + 6, player->name, 8);

      ansbuf[15] = nb_to_char(player->x, 100);
      ansbuf[16] = nb_to_char(player->x, 10);
      ansbuf[17] = nb_to_char(player->x, 1);

      ansbuf[19] = nb_to_char(player->y, 100);
      ansbuf[20] = nb_to_char(player->y, 10);
      ansbuf[21] = nb_to_char(player->y, 1);

      // sending [POSIT username xxx yyy***]
      send_msg(cli_fd, ansbuf, 25);

    }
    else if (!(check_tcp_message(reqbuf, len))) {
      printf("bad client request, discarding\n");
    }
    else if (!game->hasStarted) {
      send_string(cli_fd, "DUNNO***");
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
    // expecting [GLIS?***]
    else if (comp_keyword(reqbuf, "GLIS?") && len == 8) {
      // todo
    }
    // expecting [MALL? mess***]
    else if (comp_keyword(reqbuf, "MALL?") && len >= 10) {
      // todo
    }
    // expecting [SEND? username mess***]
    else if (comp_keyword(reqbuf, "SEND?") && len >= 19) {
      // todo
    }
    // expecting [IQUIT***]
    else if (comp_keyword(reqbuf, "IQUIT") && len == 8) {
      send_string(cli_fd, "GOBYE***");
      goto end;
    }
    else {
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
        ansbuf[14] = nb_to_char(player->score, 1000);
        ansbuf[15] = nb_to_char(player->score, 100);
        ansbuf[16] = nb_to_char(player->score, 10);
        ansbuf[17] = nb_to_char(player->score, 1);
      }
      ansbuf[6] = nb_to_char(player->x, 100);
      ansbuf[7] = nb_to_char(player->x, 10);
      ansbuf[8] = nb_to_char(player->x, 1);

      ansbuf[10] = nb_to_char(player->y, 100);
      ansbuf[11] = nb_to_char(player->y, 10);
      ansbuf[12] = nb_to_char(player->y, 1);

      // sends [MOVE! x y***] or [MOVEF x y p***]
      send_msg(cli_fd, ansbuf, size);
    }
  }

  end:

  if (isInGame) {
    game_removePlayer(game, player);
    // game removed if no players are left in it
    gameList_remove(gameList, game, RM_NOPLAYERS);
  }
  freePlayer(player);
  return NULL;
}