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
  player_t* player = (player_t*) arg;
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
    n = recv(cli_fd, keyword, 5, 0);
    if (n == -1) {
      perror("recv");
      goto end;
    }
    keyword[n] = '\0';

    // new game creation
    if (strcmp(keyword, "NEWPL") == 0) {
      // expecting [ username port***]
      n = recv(cli_fd, buf, 17, 0);
      check_tcp_message(cli_fd, buf, n);

      //TODO: check is username is alphanum
      //TODO: refactor (in player.c maybe ?)
      memmove(player->name, buf+1, MAX_NAME);
      char portstr[5];
      portstr[4] = 0;
      memmove(portstr, portstr+10, 4);
      int port = atoi(portstr);
      player->addr.sin_port = htons(port);

      // TODO: create game

      b = 0; // exits loops

    }
    // joining game
    else if (strcmp(keyword, "REGIS") == 0) {
      // expecting [ username port m***]
      n = recv(cli_fd, buf, 18, 0);
      check_tcp_message(cli_fd, buf, n);

      //TODO: check is username is alphanum
      //TODO: refactor (in player.c maybe ?)
      memmove(player->name, buf+1, MAX_NAME);
      char portstr[5];
      portstr[4] = 0;
      memmove(portstr, portstr+10, 4);
      int port = atoi(portstr);
      player->addr.sin_port = htons(port);

      //TODO:

      b = 0; // exits loops
    }
    // wrong input
    else {
      send_string(cli_fd, "DUNNO***");
      printf("bad client input, aborting\n");
      goto end;
    }
  } while (b);

  // todo : répondre à [NEWPL␣id␣port***] ou [REGIS␣id␣port␣m***] avec [REGOK␣m***], [REGNO***] ou [DUNNO***]

  end:

  close(cli_fd);
  free((int*)arg); // pas sûr de l'utilité du cast

  return NULL;
}