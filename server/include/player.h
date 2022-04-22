#ifndef PLAYER_H
#define PLAYER_H

#include <sys/socket.h>
#include <sys/types.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#define MAX_NAME 8

#define PLAYER_FREE 1
#define PLAYER_NOFREE 0

typedef struct player player_t;
typedef struct playerList playerList_t;

struct player {
  int fd;
  char name[MAX_NAME];
  struct sockaddr_in addr;
  u_int32_t x,y;
};

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
// set flag to PLAYER_FREE or PLAYER_NOFREE to free or not player
void playerList_remove(playerList_t* playerList, player_t* player, int flag);

int playerList_sendToCli(playerList_t* playerList, u_int8_t game_id, int cli_fd);

#endif