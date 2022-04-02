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

typedef struct player_t player_t;

struct player_t {
  int fd;
  char name[MAX_NAME];
  struct sockaddr_in addr;
  u_int32_t x,y;
};

// allocate memory for player. free with freePlayer()
// needs infos recieved from accept() as parameter
player_t* newPlayer(int fd, struct sockaddr_in addrinfo);

// close descriptors and free memory
void freePlayer(player_t* player);

#endif