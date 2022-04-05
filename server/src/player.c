#include "player.h"
#include "server.h"

typedef struct playerCell playerCell_t;

struct playerCell {
  player_t* player;
  playerCell_t* next;
};

struct playerList {
  u_int8_t length;
  playerCell_t* first;
};

player_t* newPlayer(int fd, struct sockaddr_in addrinfo) {
  player_t* p = malloc(sizeof(player_t));
  memset(p, 0, sizeof(player_t));
  p->fd = fd;
  p->name[0] = 0;
  p->addr.sin_family = AF_INET;
  p->addr.sin_addr.s_addr = addrinfo.sin_addr.s_addr;
  return p;
}

playerCell_t* newPlayerCell(player_t* player) {
  playerCell_t* pc = malloc(sizeof(playerCell_t));
  pc->player = player;
  pc->next = NULL;
  return pc;
}

playerList_t* newPlayerList() {
  playerList_t* pl = malloc(sizeof(playerList_t));
  pl->length = 0;
  pl->first = NULL;
  return pl;
}

void freePlayer(player_t* player) {
  if (player == NULL) return;
  close(player->fd);
  free(player);
  player = NULL;
}

void freePlayerCell(playerCell_t* pc) {
  if (pc == NULL) return;
  if (pc->next != NULL)
    freePlayerCell(pc->next);
  freePlayer(pc->player);
  free(pc);
  pc = NULL;
}

void freePlayerList(playerList_t* pl) {
  if (pl == NULL) return;
  freePlayerCell(pl->first);
  free(pl);
  pl = NULL;
}

// lock mutex before using
void player_addToList(playerList_t* playerList, player_t* player) {
  playerCell_t* pc = newPlayerCell(player);
  pc->next = playerList->first;
  playerList->first = pc;
}
