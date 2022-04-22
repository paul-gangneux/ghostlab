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

void freePlayerCell(playerCell_t* pc, int flag) {
  if (pc == NULL) return;
  if (pc->next != NULL)
    freePlayerCell(pc->next, flag);
  if (flag == PLAYER_FREE)
    freePlayer(pc->player);
  free(pc);
  pc = NULL;
}

void freePlayerList(playerList_t* pl) {
  if (pl == NULL) return;
  freePlayerCell(pl->first, PLAYER_FREE);
  free(pl);
  pl = NULL;
}

// lock mutex before using
void player_addToList(playerList_t* playerList, player_t* player) {
  playerCell_t* pc = newPlayerCell(player);
  pc->next = playerList->first;
  playerList->first = pc;
}

void playerList_remove_aux(playerCell_t* pc, player_t* player, int flag) {
  if (pc->next == NULL)
    return;
  if (pc->next->player == player) {
    playerCell_t* temp = pc->next;
    pc->next = pc->next->next;
    temp->next = NULL;
    freePlayerCell(temp, flag);
    return;
  }
  playerList_remove_aux(pc->next, player, flag);
}

// lock mutex before using
// set flag to PLAYER_FREE or PLAYER_NOFREE to free or not player
void playerList_remove(playerList_t* playerList, player_t* player, int flag) {
  if (playerList->first == NULL)
    return;
  if (playerList->first->player == player) {
    playerCell_t* pc = playerList->first;
    playerList->first = playerList->first->next;
    pc->next = NULL;
    freePlayerCell(pc, flag);
    return;
  }
  playerList_remove_aux(playerList->first, player, flag);
}

int playerList_sendToCli(playerList_t* playerList, u_int8_t game_id, int cli_fd) {
  char buf[17];
  int n;
  memmove(buf, "LIST! m s***", 12);
  buf[6] = game_id;
  buf[8] = playerList->length;
  n = send(cli_fd, buf, 12, 0);
  if (n < 0) {
    perror("send");
    return -1;
  }
  memmove(buf, "PLAYR username***", 17);
  playerCell_t* pc = playerList->first;
  while (pc != NULL) {
    memmove(buf + 6, pc->player->name, 8);
    n = send(cli_fd, buf, 17, 0);
    if (n < 0) {
      perror("send");
      return -1;
    }
    pc = pc->next;
  }
  return 0;
}