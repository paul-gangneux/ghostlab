#include "server.h"
#include "game.h"
#include "player.h"

#define MIN(a, b) (a<b?a:b)
#define MAX(a, b) (a>b?a:b)

#define send_string(sock, str) send(sock, str, strlen(str), 0)

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
  player->addr.sin_port = htons(port);

gameList_t* gameList;

int main(int argc, char* argv[]) {

  int accept_port;
  if (argc > 1)
    accept_port = atoi(argv[1]);
  else {
    accept_port = 4242;
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
    int cli_fd = accept(sock, (struct sockaddr *) &caller, &socklen);
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

void rw_buffers_initialize(reqbuf_t *buf) {
  memset(buf->readbuf, 0, READBUF_SIZE);
  memset(buf->req, 0, REQ_SIZE);
  buf->beg = 0;
  buf->end = 0;
}

// puts nect tcp request in buf->reqbuf
// returns length of request if sucessful,
// -1 on error (client disconnexion)
int nextRequest(int fd, reqbuf_t* reqbuf) {
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
    n = recv(fd, reqbuf->readbuf, READBUF_SIZE, 0);
    if (n == -1) 
      return -1;
    reqbuf->end += n;
    if (n <= 3) {
      // discarding
      reqbuf->beg = reqbuf->end;
      return n;
    }
  }

  // TODO: Lire messages de taille fixe avant de faire ce qui suit (à cause de 42 là ce fdp)

  char* tempbuf = reqbuf->readbuf + reqbuf->beg;
  n = 1;
  while (reqbuf->beg + n < reqbuf->end) {
    if (tempbuf[n-1] == '*' && tempbuf[n-2] == '*' && tempbuf[n-3] == '*') {
      break;
    }
    n++;
  }

  // n = MIN(n, REQ_SIZE); // just to be safe // test this
  
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

void* interact_with_client(void* arg) {
  int isInGame = 0;
  int isHost = 0;
  player_t* player = (player_t*) arg;
  game_t* game = NULL;
  int cli_fd = player->fd;
  int n;
  int exit_loop = 0;

  reqbuf_t reqbuf;
  rw_buffers_initialize(&reqbuf);

  char ansbuf[32];

  // sends GAMES and OGAME to client
  n = gameList_sendToCli(gameList, cli_fd);
  if (n == -1) {
    goto end;
  }

  // client creates or joins game
  while(!exit_loop) {
    n = nextRequest(cli_fd, &reqbuf);
    if (n == -1) {
      perror("nextRequest");
      goto end;
    }
    if (n == 0) {
      printf("client disconnected\n");
      goto end;
    }
    if (!(check_tcp_message(reqbuf, n))) {
      printf("bad client request, discarding\n");
    }

    else {
      // new game creation
      // expecting [NEWPL username port***]
      if (comp_keyword(reqbuf, "NEWPL") && n == 22) {
        if (isInGame) {
          send_string(cli_fd, "DUNNO***");
        } else {
          update_player_infos(player, reqbuf)
          game = newGame();
          isHost = 1;
          int id = gameList_add(gameList, game);
          if (id == -1) {
            // shouldn't happen, ever
            printf("error at addToGameList");
            goto end;
          }

          memmove(ansbuf, "REGOK 0***", 10);
          ansbuf[6] = (u_int8_t) id;
          n = send(cli_fd, ansbuf, 10, 0);
          if (n < 0) {
            perror("send");
            goto end;
          }
          
          game_addPlayer(game, player);
          //todo: verbose only
          printf("new game created with id %d\n", id);
          isInGame = 1;
        }
        
      }
      // joining game
      // expecting [REGIS username port m***]
      else if (comp_keyword(reqbuf, "REGIS") && n == 24) {
        
        update_player_infos(player, reqbuf)
        u_int8_t id = reqbuf.req[20];

        game = game_get(gameList, id);
        if (game == NULL) {
          send_string(cli_fd, "DUNNO***");
        }

        else {
          n = game_addPlayer(game, player);
          if (n == -1) {
            send_string(cli_fd, "DUNNO***");       
          }
          else {
            // redundant code sue me
            memmove(ansbuf, "REGOK 0***", 10);
            ansbuf[6] = (u_int8_t) id;
            n = send(cli_fd, ansbuf, 10, 0);
            if (n < 0) {
              perror("send");
              goto end;
            }
            isInGame = 1;
          } 
        }
      }
      // leaves game
      // expecting [UNREG***]
      else if (comp_keyword(reqbuf, "UNREG") && n == 8) {
        if (!isInGame) {
          send_string(cli_fd, "DUNNO***");
        } 
        else {
          u_int8_t id_game = game->id;
          game_removePlayer(game, player, PLAYER_NOFREE);
          if (isHost) {
            // TODO: verify there's no unfortunate player free
            gameList_remove(gameList, game);
            isHost = 0;
          }
          isInGame = 0;
          memmove(ansbuf, "UNROK 0***", 10);
          ansbuf[6] = id_game;
          n = send(cli_fd, ansbuf, 10, 0);
          if (n < 0) {
            perror("send");
            goto end;
          }
        }
      }
      // ready to start game
      // expecting [START***]
      else if (comp_keyword(reqbuf, "START") && n == 8) {
        if (!isInGame) {
          send_string(cli_fd, "DUNNO***");
        } else {
          exit_loop = 1;
        }
      }
      // asking for game length and width
      // expecting [SIZE? m***]
      else if (comp_keyword(reqbuf, "SIZE?") && n == 10) {
        u_int8_t id_game = reqbuf.req[6];
        //TODO: make this thread safe
        game_t* temp = game_get(gameList, id_game);
        if (temp == NULL) {
          send_string(cli_fd, "DUNNO***");
        }
        else {
          u_int16_t h, w;
          h = htole16(temp->h);
          w = htole16(temp->w);
          
          memmove(ansbuf, "SIZE! m hh ww***", 16);
          memmove(ansbuf + 6, &id_game, sizeof(id_game));
          memmove(ansbuf + 8, &h, sizeof(h));
          memmove(ansbuf + 11, &w, sizeof(w));
          n = send(cli_fd, ansbuf, 16, 0);
          if (n < 0) {
            perror("send");
            goto end;
          }
        }
      }
      // asking for player list
      // expecting [LIST? m***]
      else if (comp_keyword(reqbuf, "LIST?") && n == 10) {
        u_int8_t id_game = reqbuf.req[6];
        n = game_sendPlayerList(gameList, id_game, cli_fd);
      }
      // asking for game list
      // expecting [GAME? m***]
      else if (comp_keyword(reqbuf, "GAME?") && n == 10) {
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
  }

  // TODO: game has might have started, do stuff
  nextRequest(cli_fd, &reqbuf);

  end:

  // close(cli_fd); // no need ?
  if (isInGame)
    game_removePlayer(game, player, PLAYER_NOFREE);
  freePlayer(player);
  if (game != NULL && isHost)
    gameList_remove(gameList, game);
  return NULL;
}