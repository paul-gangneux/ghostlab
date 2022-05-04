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
void playerList_remove(playerList_t* playerList, player_t* player);

int playerList_sendToCli(playerList_t* playerList, u_int8_t game_id, int cli_fd);

// returns 1 if all players are ready
// returns 0 if not all player are, or if list is empty
int playerList_allReady(playerList_t* playerList);

#endif