#include "player.h"
#include "server.h"

player_t* newPlayer(int fd, struct sockaddr_in addrinfo) {
  player_t* p = malloc(sizeof(player_t));
  memset(p, 0, sizeof(player_t));
  p->fd = fd;
  p->name[0] = 0;
  p->addr.sin_family = AF_INET;
  p->addr.sin_addr.s_addr = addrinfo.sin_addr.s_addr;
  return p;
}

void freePlayer(player_t* player) {
  close(player->fd);
  free(player);
  player = NULL;
}