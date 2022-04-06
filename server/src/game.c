#include "game.h"

#define lock(x) pthread_mutex_lock(&x->mutex)
#define unlock(x) pthread_mutex_unlock(&x->mutex)

struct game {
  pthread_mutex_t mutex;
  u_int8_t id;
  u_int16_t w,h;
  u_int8_t nb_players;
  u_int8_t nb_ghosts;
  u_int8_t hasStarted;
  char multicast_port[4]; // will be 4000 + id

  playerList_t* playerList;

  char* labyrinth;
};

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
  pthread_mutex_destroy(&game->mutex);
  freePlayerList(game->playerList);
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
// remember to lock mutex before using
int addToGameList(gameList_t* gl, game_t* g) {
  if (gl->length == 0xff)
    return -1;
  gameCell_t* gc = newGameCell(g);

  lock(gl);
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
  unlock(gl);
  return x;
}

// send the [GAMES n***] and [OGAME id_game nb_players***] messages to client.
// don't forget to lock the mutex before using.
// returns 0 on success, -1 on error.
int sendGameList(gameList_t* gameList, int cli_fd) {
  lock(gameList);
  // TODO: do this in a minimal amount of send()
  int n;
  char buf[12];
  memmove(buf,"GAMES 0***", 10);
  buf[6] = gameList->length;
  n = send(cli_fd, buf, 10, 0);
  if (n<0) {
    perror("sendGameList");
    unlock(gameList);
    return -1;
  }
  gameCell_t* gc = gameList->first;
  memmove(buf, "OGAME 0 0***", 12);
  while(gc != NULL) {
    // todo : check that the game hasn't started yet
    buf[6] = gc->game->id;
    buf[8] = gc->game->nb_players;
    n = send(cli_fd, buf, 12, 0);
    if (n<0) {
      perror("sendGameList");
      unlock(gameList);
      return -1;
    }
    gc = gc->next;
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

void game_removePlayer(game_t* game, player_t* player) {
  //TODO
}

// will likely remain the same
// const char multicast_ip[15];