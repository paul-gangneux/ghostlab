#include "server.h"
#include "game.h"

#define MAX_NAME 8
#define send_string(sock, str) send(sock, str, strlen(str), 0)

//todo : move this to it's own file ?
struct client_infos {
  char pseudo[MAX_NAME];
  uint32_t ip;
  uint32_t x,y;
};

pthread_mutex_t gameList_mutex = PTHREAD_MUTEX_INITIALIZER;
gameList_t* gameList;

int main(int argc, char* argv[]) {

  int port;
  if (argc > 1)
    port = atoi(argv[1]);
  else {
    port = 4242;
  }

  int sock = init_server_socket(port);
  if (listen(sock, 0) == -1) {
    perror("listen error");
    exit(1);
  }

  printf("serveur créé sur le port %d\n", port);

  gameList = newGameList();

  socklen_t socklen = sizeof(struct sockaddr_in);

  while (1) {
    struct sockaddr_in caller;
    int cli_fd = accept(sock, (struct sockaddr *) &caller, &socklen);
    if (cli_fd == -1) {
      // pas besoin de d'arrêter le serveur sur cette erreur
      perror("accept error");
    }
    else {
      int* cli_infos = malloc(sizeof(int));
      *cli_infos = cli_fd;

      pthread_t thread;
      pthread_create(&thread, NULL, interact_with_client, (void*) cli_infos);
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
  int cli_fd = *(int*) arg;
  int n;

  pthread_mutex_lock(&gameList_mutex);
  n = sendGameList(gameList, cli_fd);
  pthread_mutex_unlock(&gameList_mutex);
  if (n == -1) {
    goto end;
  }

  char buf[64];
  n = recv(cli_fd, buf, 64, 0);

  buf[n] = 0;
  buf[63] = 0;
  printf("%s\n", buf);
  
  goto end;
  end:

  close(cli_fd);
  free((int*)arg); // pas sûr de l'utilité du cast

  return NULL;
}