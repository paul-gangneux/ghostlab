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

#define nb_to_char(nb, factor) ((nb / factor) % 10) + '0'

extern int verbose;
extern int very_verbose;

extern const char* multicast_ip_address;

// renvoie le FD du socket TCP créé
int init_server_socket(int port);
void* interact_with_client(void* arg);

#endif