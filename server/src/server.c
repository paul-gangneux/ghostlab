#include "server.h"
#include "game.h"
#include "player.h"

#define send_string(sock, str) send(sock, str, strlen(str), 0)

// checks that tcp message has been properly recieved 
// and that the message ends with ***
#define check_tcp_message(cli_fd, buf, n)\
  if (n == -1) {\
    perror("recv");\
    goto end;\
  }\
  if (n < 3 || buf[n] != '*' || buf[n-1] != '*' || buf[n-2] != '*') {\
    send_string(cli_fd, "DUNNO***");\
    printf("bad client input, aborting\n");\
    goto end;\
  }

#define read_keyword(cli_fd, keyword, n)\
  n = recv(cli_fd, keyword, 5, 0);\
  if (n == -1) {\
    perror("recv");\
    goto end;\
  }\
  keyword[n] = '\0';

// TODO: check is username is alphanum
#define updatePlayerInfos(player, buf)\
  memmove(player->name, buf+1, MAX_NAME);\
  char portstr[5];\
  portstr[4] = 0;\
  memmove(portstr, buf+10, 4);\
  int port = atoi(portstr);\
  player->addr.sin_port = htons(port);

pthread_mutex_t gameList_mutex = PTHREAD_MUTEX_INITIALIZER;
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
  // addToGameList(gameList, test_game);
  // game_t* test_game2 = newGame();
  // addToGameList(gameList, test_game2);

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

void* interact_with_client(void* arg) {
  int isHost = 0;
  player_t* player = (player_t*) arg;
  game_t* game = NULL;
  int cli_fd = player->fd;
  int n;
  int b = 0; // boolean

  // sends GAMES and OGAME to client
  pthread_mutex_lock(&gameList_mutex);
  n = sendGameList(gameList, cli_fd);
  pthread_mutex_unlock(&gameList_mutex);
  if (n == -1) {
    goto end;
  }

  char buf[64];
  char keyword[6];
  keyword[5] = '\0';

  // client creates or joins game
  do {
    read_keyword(cli_fd, keyword, n);

    // new game creation
    if (strcmp(keyword, "NEWPL") == 0) {
      // expecting [ username port***]
      n = recv(cli_fd, buf, 17, 0);
      check_tcp_message(cli_fd, buf, n);
      updatePlayerInfos(player, buf)

      game = newGame();
      isHost = 1;
      
      
      pthread_mutex_lock(&gameList_mutex);
      int id = addToGameList(gameList, game);
      pthread_mutex_unlock(&gameList_mutex);

      if (id == -1) {
        // shouldn't happen, ever
        printf("error at addToGameList");
        goto end;
      }

      // todo: refactor
      memmove(buf, "REGOK 0***", 10);
      buf[6] = (u_int8_t) id;
      n = send(cli_fd, buf, 10, 0);
      if (n<0) {
        perror("send");
        goto end;
      }

      b = 0; // exits loops
    }
    // joining game
    else if (strcmp(keyword, "REGIS") == 0) {
      // expecting [ username port m***]
      n = recv(cli_fd, buf, 18, 0);
      check_tcp_message(cli_fd, buf, n);
      updatePlayerInfos(player, buf);

      u_int8_t id = buf[15];

      // todo, move these locks in game.c
      pthread_mutex_lock(&gameList_mutex);
      game = game_get(gameList, id);
      pthread_mutex_unlock(&gameList_mutex);
      if (game == NULL) {
        b = 1;
        send_string(cli_fd, "DUNNO***");
      }
      else {

        //todo: check for failure (if game already started)
        n = game_addPlayer(game, player);
        if (n == -1) {
          b = 1;
          send_string(cli_fd, "DUNNO***");
        }
        else {
          memmove(buf, "REGOK 0***", 10);
          buf[6] = (u_int8_t) id; // useless cast sue me 
          n = send(cli_fd, buf, 10, 0);
          if (n<0) {
            perror("send");
            goto end;
          }
          b = 0; // exits loops
        } 
      }
    }
    // wrong input
    else {
      send_string(cli_fd, "DUNNO***");
      printf("bad client input, aborting\n");
      goto end;
    }

  } while (b);

  // TODO: wait for start*** or disconnect

  recv(cli_fd, buf, 5, 0);

  end:

  close(cli_fd); // no need ? better be safe
  game_removePlayer(game, player);
  freePlayer(player);
  if (game != NULL && isHost)
    //TODO : remove game from gamelist
    freeGame(game); 

  return NULL;
}