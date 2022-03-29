#ifndef SERVER_H
#define SERVER_H

#include <sys/socket.h>
#include <sys/types.h> 
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <string.h>

// renvoie le FD du socket TCP créé
int init_server_socket(int port);
void* interact_with_client(void* arg);

#endif