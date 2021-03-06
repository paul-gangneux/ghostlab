#ifndef PLAYER_H
#define PLAYER_H

#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#define MAX_NAME 8

typedef struct player player_t;
typedef struct playerList playerList_t;

struct player {
  int fd;
  int pipe[2]; // sert à envoyer des "signaux" au joueur
  char name[MAX_NAME];
  struct sockaddr_in addr;
  int is_ready;
  int x, y;
  int score;
};

#define END_THREAD 1
#define NO_END_THREAD 0

#include "server.h"
#include "communication.h"

// allocate memory for player. free with freePlayer()
// needs infos recieved from accept() as parameter
player_t* newPlayer(int fd, struct sockaddr_in addrinfo);
playerList_t* newPlayerList();

// close descriptors and free memory
void freePlayer(player_t* player);
// yeets all
void freePlayerList(playerList_t* pl);
// lock mutex before using
void player_addToList(playerList_t* playerList, player_t* player);

// lock mutex before using
// returns 1 on success, 0 on failure
// ends player thread is endThread != 0
int playerList_remove(playerList_t* playerList, player_t* player, int endThread);

int playerList_sendToCli(playerList_t* playerList, u_int8_t game_id, int cli_fd);

// returns 1 if all players are ready
// returns 0 if not all player are, or if list is empty
int playerList_allReady(playerList_t* playerList);

// applies f to all player within playerList
void playerList_forAll(playerList_t* playerList, void (*f)(player_t*));

// sends [GLIS! s***] and [GPLYR username x y p***]
// returns 1 on success, 0 on failure
// lock mutex before using
int playerList_sendToCli_AllInfos(playerList_t* playerList, int cli_fd);

int playerList_hasPlayerWithSameId(playerList_t* playerList, player_t* player);

// returns null if player doesn't exist
player_t* playerList_getPlayer(playerList_t* playerlist, char name[MAX_NAME]);

player_t* playerList_getPlayerWithMaxScore(playerList_t* playerList);

// returns 1 if coordonates or player are the same as coordonates
// of another player in playerList. returns 0 otherwise.
int playerList_inAnotherPlayer(playerList_t* playerList, player_t* player);

int playerList_inAPlayer(playerList_t* playerList, int x, int y);

#endif