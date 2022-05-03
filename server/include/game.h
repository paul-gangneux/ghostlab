#ifndef GAME_H
#define GAME_H

#include <sys/types.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <sys/socket.h>
#include <pthread.h>

#include "player.h"

// #define GAME_DESC_SIZE 12

#define MV_NONE 0
#define MV_UP 1
#define MV_DOWN 2
#define MV_RIGHT 3
#define MV_LEFT 4

typedef struct game game_t;
typedef struct gameList gameList_t;
typedef struct ghost ghost_t;

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
  ghost_t* ghosts;
};

// send the [GAMES n***] and [OGAME id_game nb_players***] messages to client.
// returns 0 on success, -1 on error.
int gameList_sendToCli(gameList_t* gameList, int cli_fd);

// allocate memory for a game. free with freeGame()
// does not set id and port to their correct values
game_t* newGame();

// allocate memory for a game list. free with freeGameList()
gameList_t* newGameList();

void freeGame(game_t* game);

void freeGameList(gameList_t* gameList);

// add game to the game list, returns game id on success, -1 on failure.
// sets game->id and game->multicast_port appropriately.
int gameList_add(gameList_t* gl, game_t* g);

// returns -1 on failure, 0 on success
int game_addPlayer(game_t* game, player_t* player);

void game_removePlayer(game_t* game, player_t* player);

// returns NULL on failure
game_t* game_get(gameList_t* gameList, u_int8_t id);

// free game
void gameList_remove(gameList_t *gameList, game_t* game);

// send the [LIST! m s***] and [PLAYR id***] messages to client.
// returns -1 on failure, 0 on success
int game_sendPlayerList(gameList_t* gameList, u_int8_t game_id, int cli_fd);

// starts game in a new thread if all players are ready.
// demands that at least one player is registered
void game_startIfAllReady(game_t* game);

// returns 1 if the player captured at least one ghost, else returns 0
int game_movePlayer(game_t* game, player_t* player, int amount, int direction);

#endif