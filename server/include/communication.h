#ifndef COMMUNICATION_H
#define COMMUNICATION_H

#define READBUF_SIZE 1024
#define REQ_SIZE 512

#define REQ_DISCONNECTION 0
#define REQ_ERROR -1
#define REQ_FORCE_END -2
#define REQ_GAME_START -3
#define REQ_GAME_END -4

typedef struct reqbuf {
  char readbuf[READBUF_SIZE];
  char req[REQ_SIZE];
  int beg, end; // cursors
} reqbuf_t;

#include "server.h"
#include "player.h"
#include "game.h"

#define cmp3chars(buf, str) (buf[0] == str[0] && buf[1] == str[1] && buf[2] == str[2])

// returns 1 on success, 0 on failure
int send_msg(int fd, char* buf, int length);

// returns 1 on success, 0 on failure
int send_string(int fd, char* str);

// sets everything to 0
void rw_buffers_initialize(reqbuf_t* buf);

// puts nect tcp request in buf->reqbuf
// returns length of request if sucessful, or a negative or zero value
// for special occasions. these values are:
// REQ_ERROR on error
// REQ_DISCONNECTION if client disconnected
// REQ_END if server wants player thread to end
// REQ_GAME_START if server wants to inform that the game has started
// REQ_GAME_END if server wants to inform that the game has ended
int next_request(player_t* player, reqbuf_t* reqbuf);

// sends [WELCO m h w f ip port***] and
// [POSIT username x y***] messages
// takes a buffer of size >= 39 as argument
// returns 1 on success, 0 on failure
int send_welcome_msg(player_t* player, game_t* game, char* ansbuf);

// allows sending of UDP messages
// only call this once
void init_udp_sock();

// send UDP message to player adress
// make sure to call init_udp_sock() before using
// returns 1 on success, 0 on failure
int send_msg_to(player_t* player, char* buf, int len);

// multicasts UDP message game multicast adress
// make sure to call init_udp_sock() before using
// returns 1 on success, 0 on failure
int send_msg_multicast(game_t* game, char* buf, int len);

#endif