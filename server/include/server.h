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

#include "game.h"
#include "player.h"

extern int verbose;
extern int very_verbose;

extern const char* multicast_address;

// check_error must be defined in each file
#define send_msg_specify_n(dest_fd, msg_buf, msg_length, n)\
  if (very_verbose) {\
    n = write(STDOUT_FILENO, "-> ", 3);\
    check_error(n)\
    n = write(STDOUT_FILENO, msg_buf, msg_length);\
    check_error(n)\
    n = write(STDOUT_FILENO, "\n", 1);\
    check_error(n)\
  }\
  n = send(dest_fd, msg_buf, msg_length, 0);\
  check_error(n)

#define send_msg(dest_fd, msg_buf, msg_length) send_msg_specify_n(dest_fd, msg_buf, msg_length, n)

// renvoie le FD du socket TCP créé
int init_server_socket(int port);
void* interact_with_client(void* arg);

#endif