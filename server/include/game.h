#ifndef GAME_H
#define GAME_H

#include <sys/types.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <stdio.h>
#include <sys/socket.h>
#include <pthread.h>
#include <netdb.h>
#include <arpa/inet.h>

// #define GAME_DESC_SIZE 12

#define MV_NONE 0
#define MV_UP 1
#define MV_DOWN 2
#define MV_RIGHT 3
#define MV_LEFT 4

#define RM_FORCE 0
#define RM_NOPLAYERS 1

typedef struct game game_t;
typedef struct gameList gameList_t;
typedef struct ghost ghost_t;

#include "player.h"
#include "server.h"
#include "maze.h"

struct game {
  pthread_mutex_t mutex;
  int pipe1[2]; // pour parler au thread de jeu
  int pipe2[2]; // réponses du thead de jeu
  u_int8_t id;
  u_int16_t w, h;
  u_int8_t nb_players;
  u_int8_t nb_ghosts;
  u_int8_t hasStarted;
  char multicast_port[4]; // will be 4000 + id
  playerList_t* playerList;
  char* maze;
  ghost_t* ghosts;
  struct sockaddr_in multicast_addr;
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
// will fail if there's another user with the same name in that game
int game_addPlayer(gameList_t* gameList, u_int8_t game_id, player_t* player);

// remove player from game
// and asks for player thread to end if endThread != 0
void game_removePlayer(game_t* game, player_t* player, int endThread);

// returns NULL on failure
game_t* game_get(gameList_t* gameList, u_int8_t id);

// removes game from gameList and free memory
// use with option RM_NOPLAYERS to only remove if no players are in the game.
// use with option RM_FORCE to remove no matter what
// will free associated playerlist and ask player to disconnect
// returns 1 if game has been deleted, 0 otherwise
int gameList_remove(gameList_t* gameList, game_t* game, int option);

// send the [LIST! m s***] and [PLAYR id***] messages to client.
// returns -1 on failure, 0 on success
int game_sendPlayerList(gameList_t* gameList, u_int8_t game_id, int cli_fd);

// starts game in a new thread if all players are ready.
// demands that at least one player is registered
void game_startIfAllReady(game_t* game);

// returns 1 if the player captured at least one ghost, else returns 0
int game_movePlayer(game_t* game, player_t* player, int amount, int direction);

// returns NULL on failure
game_t* game_getSize(gameList_t* gameList, u_int8_t id_game, u_int16_t* h, u_int16_t* w);

void game_randomizePosition(game_t* game, player_t* player);

// sends [GLIS! s***] and [GPLYR username x y p***]
// returns 1 on success, 0 on failure
int game_sendPlayerList_AllInfos(game_t* game, int cli_fd);

// send n bytes from buf to dest user via UDP
// returns 1 on success, 0 on failure
int game_sendMessageToOnePlayer(game_t* game, char destId[8], char* buf, int len);

void game_endIfNoGhost(game_t* game);

void game_getSurroundings(game_t* game, player_t* player, char* buf);

#endif