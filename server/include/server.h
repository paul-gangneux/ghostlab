#ifndef SERVER_H
#define SERVER_H

#include <sys/socket.h>
#include <sys/types.h> 
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <pthread.h>
#include <endian.h>
#include <getopt.h>
#include <poll.h>

#include "player.h"
#include "game.h"
#include "communication.h"

#define MIN(a, b) (a<b?a:b)
#define MAX(a, b) (a>b?a:b)

#define nb_to_char(nb, factor) ((nb / factor) % 10) + '0'

#define mv_num3toBuf(buf, ind, num)\
  buf[ind] = nb_to_char(num, 100);\
  buf[ind + 1] = nb_to_char(num, 10);\
  buf[ind + 2] = nb_to_char(num, 1)

#define mv_num4toBuf(buf, ind, num)\
  buf[ind] = nb_to_char(num, 1000);\
  buf[ind + 1] = nb_to_char(num, 100);\
  buf[ind + 2] = nb_to_char(num, 10);\
  buf[ind + 3] = nb_to_char(num, 1)

extern int verbose;
extern int very_verbose;
extern int print_mazes;
extern int easy_mazes;
extern int ghost_delay;

extern const char* multicast_ip_address;

// renvoie le FD du socket TCP créé
int init_server_socket(int port);
void* interact_with_client(void* arg);

#endif