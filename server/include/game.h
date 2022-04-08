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

typedef struct game game_t;
typedef struct gameList gameList_t;

// send the [GAMES n***] and [OGAME id_game nb_players***] messages to client.
// returns 0 on success, -1 on error.
int sendGameList(gameList_t* gameList, int cli_fd);

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

// doesn't free player
void game_removePlayer(game_t* game, player_t* player);

// returns NULL on failure
game_t* game_get(gameList_t* gameList, u_int8_t id);

// free game
void gameList_remove(gameList_t *gameList, game_t* game);

#endif