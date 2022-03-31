#include "server.h"

#define MAX_NAME 8
#define send_string(sock, str) send(sock, str, strlen(str), 0)

struct client_infos {
  char pseudo[MAX_NAME];
  uint32_t ip;
  uint32_t x,y;
};

pthread_mutex_t lock = PTHREAD_MUTEX_INITIALIZER;

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

  socklen_t socklen = sizeof(struct sockaddr_in);

  while (1) {
    struct sockaddr_in caller;
    int cli_fd = accept(sock, (struct sockaddr *) &caller, &socklen);
    if (cli_fd == -1) {
      // pas besoin de d'arrêter le serveur sur cette erreur
      perror("accept error");
    }
    else {
      uint32_t* cli_infos = malloc(sizeof(uint32_t));
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
  uint32_t cli_fd = *(uint32_t*) arg;

  // TODO
  
  goto end;
  end:

  close(cli_fd);
  free((uint32_t*)arg); // pas sûr de l'utilité du cast

  return NULL;
}