#include "game.h"

#define lock(x) pthread_mutex_lock(&x->mutex)
#define unlock(x) pthread_mutex_unlock(&x->mutex)

typedef struct gameCell gameCell_t;

struct gameCell {
  game_t* game;
  gameCell_t* next;
};

struct gameList {
  pthread_mutex_t mutex;
  u_int8_t length;
  gameCell_t* first;
};

// generate and return the labyrinth
// sets w and h appropriately
char* newLabyrinth(u_int16_t* w, u_int16_t* h) {
  *w = 8;
  *h = 6;
  char* lab = (char*) malloc((*w) * (*h) * sizeof(char));

  //TODO : generate walls
  // placeholder labyrinth:
  char temp[49] = 
    "01000000"
    "01010101"
    "00010001"
    "11011100"
    "00001000"
    "00100011";
  memmove(lab, temp, 48);

  return lab;
}

// allocate memory for a game. free with freeGame()
// does not set id and port to their correct values
game_t* newGame() {
  game_t* g = (game_t*) malloc(sizeof(game_t));
  pthread_mutex_init(&g->mutex, NULL);
  g->id = 0;
  g->nb_players = 0;
  g->nb_ghosts = 0; // todo?
  g->multicast_port[0] = '4';
  g->multicast_port[1] = '0';
  g->multicast_port[2] = '0';
  g->multicast_port[3] = '0';
  g->hasStarted = 0;
  g->labyrinth = newLabyrinth(&g->w, &g->h);
  g->playerList = newPlayerList();
  return g;
}

// allocate memory for a game list. free with freeGameList()
gameList_t* newGameList() {
  gameList_t* gl = (gameList_t*) malloc(sizeof(gameList_t));
  pthread_mutex_init(&gl->mutex, NULL);
  gl->length = 0;
  gl->first = NULL;
  return gl;
}

// allocate memory for a game cell. free with freeGameCell()
gameCell_t* newGameCell(game_t* g) {
  gameCell_t* gc = (gameCell_t*) malloc(sizeof(gameCell_t));
  gc->game = g;
  gc->next = NULL;
  return gc;
}

void freeGame(game_t* game) {
  lock(game);
  freePlayerList(game->playerList);
  unlock(game);
  pthread_mutex_destroy(&game->mutex);
  free(game->labyrinth);
  free(game);
  game = NULL;
}

void freeGameCell(gameCell_t* gameCell) {
  if (gameCell->next != NULL)
    freeGameCell(gameCell->next);
  freeGame(gameCell->game);
  free(gameCell);
  gameCell = NULL;
}

void freeGameList(gameList_t* gameList) {
  pthread_mutex_destroy(&gameList->mutex);
  if (gameList->first != NULL) {
    freeGameCell(gameList->first);
  }
  free(gameList);
  gameList = NULL;
}

int insertIntoList(gameCell_t* curr, gameCell_t* gc, u_int8_t n) {
  if (curr->next == NULL || n < curr->next->game->id) {
    gc->next = curr->next;
    curr->next = gc;
    gc->game->id = n;
    // kinda dirty but works
    gc->game->multicast_port[1] += n / 100;
    gc->game->multicast_port[2] += (n % 100) / 10;
    gc->game->multicast_port[3] += (n % 10);
    return n;
  }
  return insertIntoList(curr->next, gc, n+1);
}

// add game to the game list, returns game id on success, -1 on failure.
// sets game->id and game->multicast_port appropriately.
int gameList_add(gameList_t* gl, game_t* g) {
  lock(gl);
  if (gl->length == 0xff) {
    unlock(gl);
    return -1;
  }
  gameCell_t* gc = newGameCell(g);

  if (gl->first == NULL || gl->first->game->id > 1) {
    gc->next = gl->first;
    gc->game->id = 1;
    gc->game->multicast_port[3] = '1';
    gl->length += 1;
    gl->first = gc;
    unlock(gl);
    return 1;
  }
  int x = insertIntoList(gl->first, gc, 2);
  gl->length += 1;
  unlock(gl);
  return x;
}

u_int8_t get_nb_of_started_games_aux(gameCell_t* cell, u_int8_t i) {
  if (cell == NULL)
    return i;
  if (cell->game->hasStarted) {
    return get_nb_of_started_games_aux(cell->next, i+1);
  }
  return get_nb_of_started_games_aux(cell->next, i);
}

u_int8_t get_nb_of_started_games(gameList_t* gameList) {
  if (gameList == NULL)
    return 0;
  return get_nb_of_started_games_aux(gameList->first, 0);
}

#define NB_MAX 10
#define OGAME_LEN 12

// send the [GAMES n***] and [OGAME id_game nb_players***] messages to client.
// only send games that haven't started yet
// returns 0 on success, -1 on error.
int gameList_sendToCli(gameList_t* gameList, int cli_fd) {
  lock(gameList);
  int n, i;
  char buf[OGAME_LEN * NB_MAX];
  memmove(buf,"GAMES 0***", 10);
  buf[6] = gameList->length - get_nb_of_started_games(gameList);
  n = send(cli_fd, buf, 10, 0);
  if (n < 0) {
    perror("sendGameList");
    unlock(gameList);
    return -1;
  }

  gameCell_t* gc = gameList->first;
  for (i = 0; i < NB_MAX; i++)
    memmove(buf + i * OGAME_LEN, "OGAME 0 0***", OGAME_LEN);
  i = 0;
  while(gc != NULL) {
    if (!gc->game->hasStarted) {
      buf[i * OGAME_LEN + 6] = gc->game->id;
      buf[i * OGAME_LEN + 8] = gc->game->nb_players;
      i++;
    }
    gc = gc->next;
    if (i == NB_MAX) {
      n = send(cli_fd, buf, OGAME_LEN * NB_MAX, 0);
      if (n < 0) {
        perror("sendGameList");
        unlock(gameList);
        return -1;
      }
      i = 0;
    }
  }

  if (i > 0) {
    n = send(cli_fd, buf, OGAME_LEN * i, 0);
    if (n < 0) {
      perror("sendGameList");
      unlock(gameList);
      return -1;
    }
  }

  unlock(gameList);
  return 0;
}

game_t* aux_game_get(gameCell_t* gc, u_int8_t id) {
  if (gc == NULL)
    return NULL;
  if (gc->game->id == id)
    return gc->game;
  return aux_game_get(gc->next, id);
}

// returns NULL on failure
game_t* game_get(gameList_t* gameList, u_int8_t id) {
  lock(gameList);
  game_t* game = aux_game_get(gameList->first, id);
  unlock(gameList);
  return game;
}

// returns -1 on failure, 0 on success
int game_addPlayer(game_t* game, player_t* player) {
  lock(game);
  int i = 0;
  if (game->hasStarted)
    i = -1;
  else
    player_addToList(game->playerList, player);
  unlock(game);
  return i;
}

// free player or not with flag set to PLAYER_FREE or PLAYER_NOFREE
void game_removePlayer(game_t* game, player_t* player) {
  lock(game);
  playerList_remove(game->playerList, player);
  unlock(game);
}

// returns 0 on failure, 1 on success
int gameList_remove_aux(gameCell_t* gc, game_t* game) {
  if (gc->next == NULL)
    return 0;
  if (gc->next->game == game) {
    gameCell_t* temp = gc->next;
    gc->next = gc->next->next;
    temp->next = NULL;
    freeGameCell(temp);
    return 1;
  }
  return gameList_remove_aux(gc->next, game);
}

// free game
void gameList_remove(gameList_t *gameList, game_t* game) {
  lock(gameList);
  if (gameList->first == NULL) {
    unlock(gameList);
    return;
  }
  if (gameList->first->game == game) {
    gameCell_t* gc = gameList->first;
    gameList->first = gameList->first->next;
    gc->next = NULL;
    freeGameCell(gc);
    gameList->length -= 1;
    unlock(gameList);
    return;
  }
  if(gameList_remove_aux(gameList->first, game))
    gameList->length -= 1;
  unlock(gameList);
}

// send the [LIST! m s***] and [PLAYR id***] messages to client.
// returns -1 on failure, 0 on success
int game_sendPlayerList(gameList_t* gameList, u_int8_t game_id, int cli_fd) {
  game_t* game = game_get(gameList, game_id);
  if(game == NULL)
    return -1;
  lock(game);
  int n = playerList_sendToCli(game->playerList, game_id, cli_fd);
  unlock(game);
  return n;
}

// will likely remain the same
// const char multicast_ip[15];