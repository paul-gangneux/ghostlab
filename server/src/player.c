#include "player.h"

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
  int n = pipe(p->pipe);
  if (n < 0) {
    perror("pipe");
    free(p);
    return NULL;
  }
  p->name[0] = 0;
  p->addr.sin_family = AF_INET;
  p->addr.sin_addr.s_addr = addrinfo.sin_addr.s_addr;
  p->is_ready = 0;
  p->x = -1;
  p->y = -1;
  p->score = 0;
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
  close(player->pipe[0]);
  close(player->pipe[1]);
  free(player);
  player = NULL;
}

void player_endThread(player_t* player) {
  int n = write(player->pipe[1], "end", 3);
  if (n < 0) {
    perror("write");
  }
}

// free all subsequent player cells.
// set pc->next to NULL to avoid this
void freePlayerCell(playerCell_t* pc) {
  if (pc == NULL) return;
  if (pc->next != NULL)
    freePlayerCell(pc->next);
  player_endThread(pc->player);
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
  playerList->length += 1;
}

// returns 0 on failure, 1 on success
int playerList_remove_aux(playerCell_t* pc, player_t* player) {
  if (pc->next == NULL)
    return 0;
  if (pc->next->player == player) {
    playerCell_t* temp = pc->next;
    pc->next = pc->next->next;
    temp->next = NULL;
    freePlayerCell(temp);
    return 1;
  }
  return playerList_remove_aux(pc->next, player);
}

// lock mutex before using
// returns 1 on success, 0 on failure
int playerList_remove(playerList_t* playerList, player_t* player) {
  if (playerList->first == NULL)
    return 0;
  if (playerList->first->player == player) {
    playerCell_t* pc = playerList->first;
    playerList->first = playerList->first->next;
    pc->next = NULL;
    freePlayerCell(pc);
    playerList->length -= 1;
    return 1;
  }
  if (playerList_remove_aux(playerList->first, player)) {
    playerList->length -= 1;
    return 1;
  }
  return 0;
}

int playerList_sendToCli(playerList_t* playerList, u_int8_t game_id, int cli_fd) {
  char buf[17];
  memmove(buf, "LIST! m s***", 12);
  buf[6] = game_id;
  buf[8] = playerList->length;
  if (!send_msg(cli_fd, buf, 12))
    return -1;
  memmove(buf, "PLAYR username***", 17);
  playerCell_t* pc = playerList->first;
  while (pc != NULL) {
    memmove(buf + 6, pc->player->name, 8);
    if (!send_msg(cli_fd, buf, 17))
      return -1;
    pc = pc->next;
  }
  return 0;
}

// sends [GLIS! s***] and [GPLYR username x y p***]
// returns 1 on success, 0 on failure
// lock mutex before using
int playerList_sendToCli_AllInfos(playerList_t* playerList, int cli_fd) {
  char buf[30];
  memmove(buf, "GLIS! s***", 10);
  buf[6] = playerList->length;
  if (!send_msg(cli_fd, buf, 10))
    return 0;

  memmove(buf, "GPLYR username xxx yyy pppp***", 30);
  playerCell_t* pc = playerList->first;
  while (pc != NULL) {
    memmove(buf + 6, pc->player->name, 8);

    buf[15] = nb_to_char(pc->player->x, 100);
    buf[16] = nb_to_char(pc->player->x, 10);
    buf[17] = nb_to_char(pc->player->x, 1);

    buf[19] = nb_to_char(pc->player->y, 100);
    buf[20] = nb_to_char(pc->player->y, 10);
    buf[21] = nb_to_char(pc->player->y, 1);

    buf[23] = nb_to_char(pc->player->score, 1000);
    buf[24] = nb_to_char(pc->player->score, 100);
    buf[25] = nb_to_char(pc->player->score, 10);
    buf[26] = nb_to_char(pc->player->score, 1);

    if (!send_msg(cli_fd, buf, 30))
      return 0;
    pc = pc->next;
  }
  return 1;
}

int playerList_allReady_aux(playerCell_t* pc) {
  if (pc == NULL)
    return 1;
  if (pc->player->is_ready == 0)
    return 0;
  return playerList_allReady_aux(pc->next);
}

// returns 1 if all players are ready
// returns 0 if not all player are, or if list is empty
int playerList_allReady(playerList_t* playerList) {
  if (playerList->first == NULL)
    return 0;
  return playerList_allReady_aux(playerList->first);
}

void playerList_forAll_aux(playerCell_t* pc, void (*f)(player_t*)) {
  if (pc == NULL)
    return;
  f(pc->player);
  playerList_forAll_aux(pc->next, f);
}

void playerList_forAll(playerList_t* playerList, void (*f)(player_t*)) {
  playerList_forAll_aux(playerList->first, f);
}

int namesAreEqual(player_t* p1, char name[MAX_NAME]) {
  for (int i = 0; i < MAX_NAME; i++) {
    if (p1->name[i] != name[i])
      return 0;
  }
  return 1;
}

player_t* playerList_getPlayer_aux(playerCell_t* pc, char name[MAX_NAME]) {
  if (pc == NULL)
    return NULL;
  if (namesAreEqual(pc->player, name))
    return pc->player;
  return playerList_getPlayer_aux(pc->next, name);
}

// returns null if player doesn't exist
player_t* playerList_getPlayer(playerList_t* playerlist, char name[MAX_NAME]) {
  return playerList_getPlayer_aux(playerlist->first, name);
}

int playerList_hasPlayerWithSameId(playerList_t* playerList, player_t* player) {
  return (playerList_getPlayer(playerList, player->name) != NULL);
}

player_t* playerList_getPlayerWithMaxScore_aux(playerCell_t* pc, player_t* maxPlayer) {
  if (pc == NULL)
    return maxPlayer;
  if (pc->player->score > maxPlayer->score)
    maxPlayer = pc->player;
  return playerList_getPlayerWithMaxScore_aux(pc->next, maxPlayer);
}

player_t* playerList_getPlayerWithMaxScore(playerList_t* playerList) {
  if (playerList->first == NULL)
    return NULL;
  return playerList_getPlayerWithMaxScore_aux(playerList->first, playerList->first->player);
}

int inAnotherPlayer_aux(playerCell_t* pc, player_t* p) {
  if (pc == NULL)
    return 0;
  if (pc->player != p &&
    pc->player->x == p->x &&
    pc->player->y == p->y)
    return 1;
  return inAnotherPlayer_aux(pc->next, p);
}

int playerList_inAnotherPlayer(playerList_t* playerList, player_t* player) {
  return inAnotherPlayer_aux(playerList->first, player);
}

int playerList_inAPlayer(playerList_t* playerList, int x, int y) {
  player_t p;
  p.x = x;
  p.y = y;
  return playerList_inAnotherPlayer(playerList, &p);
}

