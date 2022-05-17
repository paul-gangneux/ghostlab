#include "communication.h"

#define MIN(a, b) (a<b?a:b)
// #define MAX(a, b) (a>b?a:b)

#define write_stdout_and_check_error(buf, len)\
  if (write(STDOUT_FILENO, buf, len) < 0) perror("write")

#define print_out_msg(buf, len)\
  write_stdout_and_check_error(" -> ", 4);\
  write_stdout_and_check_error(buf, len);\
  write_stdout_and_check_error("\n", 1)

#define print_incoming_req(reqbuf, len)\
  write_stdout_and_check_error(" <- ", 4);\
  write_stdout_and_check_error(reqbuf->req, len);\
  write_stdout_and_check_error("\n", 1)

int udp_sock = -1;
pthread_mutex_t udp_sock_mutex;

// returns 1 on success, 0 on failure
int send_msg(int fd, char* buf, int length) {
  if (very_verbose) {
    print_out_msg(buf, length);
  }

  if (send(fd, buf, length, 0) < 0)
    return 0;

  return 1;
}

// returns 1 on success, 0 on failure
int send_string(int fd, char* str) {
  return send_msg(fd, str, strlen(str));
}

// sends [WELCO m h w f ip port***] and
// [POSIT username x y***] messages
// takes a buffer of size >= 39 as argument
// returns 1 on success, 0 on failure
int send_welcome_msg(player_t* player, game_t* game, char* ansbuf) {
  memmove(ansbuf, "WELCO m hh ww f ip_.___.___.___ port***", 39);
  ansbuf[6] = game->id;
  u_int16_t h = htole16(game->h);
  u_int16_t w = htole16(game->w);
  memmove(ansbuf + 8, &h, sizeof(u_int16_t));
  memmove(ansbuf + 11, &w, sizeof(u_int16_t));
  ansbuf[14] = game->nb_ghosts;
  memmove(ansbuf + 16, multicast_ip_address, 15);
  memmove(ansbuf + 32, game->multicast_port, 4);

  // sending [WELCO m h w f ip port***]
  if (!send_msg(player->fd, ansbuf, 39))
    return 0;

  memmove(ansbuf, "POSIT username xxx yyy***", 25);
  memmove(ansbuf + 6, player->name, 8);

  ansbuf[15] = nb_to_char(player->x, 100);
  ansbuf[16] = nb_to_char(player->x, 10);
  ansbuf[17] = nb_to_char(player->x, 1);

  ansbuf[19] = nb_to_char(player->y, 100);
  ansbuf[20] = nb_to_char(player->y, 10);
  ansbuf[21] = nb_to_char(player->y, 1);

  // sending [POSIT username xxx yyy***]
  if (!send_msg(player->fd, ansbuf, 25))
    return 0;

  return 1;
}

void rw_buffers_initialize(reqbuf_t* buf) {
  memset(buf->readbuf, 0, READBUF_SIZE);
  memset(buf->req, 0, REQ_SIZE);
  buf->beg = 0;
  buf->end = 0;
}

// puts nect tcp request in buf->reqbuf
// returns length of request if sucessful, or a negative or zero value
// for special occasions. these values are:
// REQ_ERROR on error
// REQ_DISCONNECTION if client disconnected
// REQ_END if server wants player thread to end
// REQ_GAME_START if server wants to inform that a game has started
int next_request(player_t* player, reqbuf_t* reqbuf) {
  int n = 0;
  if (reqbuf->beg >= READBUF_SIZE - REQ_SIZE) {
    memmove(reqbuf->readbuf, reqbuf->readbuf + reqbuf->beg, reqbuf->end - reqbuf->beg);
    reqbuf->end -= reqbuf->beg;
    reqbuf->beg = 0;
  }

  if (reqbuf->beg == reqbuf->end) {
    reqbuf->beg = 0;
    reqbuf->end = 0;

    struct pollfd pollfd[2] = {
      {.fd = player->fd, .events = POLLIN},
      {.fd = player->pipe[0], .events = POLLIN}
    };
    poll(pollfd, 2, -1);

    if (pollfd[1].revents & POLLIN) {
      char buf[3] = { 0, 0, 0 };
      n = read(player->pipe[0], buf, 3);
      if (n == 3 && cmp3chars(buf, "end"))
        return REQ_FORCE_END; // force end
      if (n == 3 && cmp3chars(buf, "bgn"))
        return REQ_GAME_START; // game begins
      if (n == 3 && cmp3chars(buf, "fin"))
        return REQ_GAME_END; // game ended
    }

    n = recv(player->fd, reqbuf->readbuf, READBUF_SIZE, 0);

    if (n == -1)
      return REQ_ERROR;
    if (n == 0)
      return REQ_DISCONNECTION;

    reqbuf->end += n;
    if (n <= 3) {
      // discarding
      reqbuf->beg = reqbuf->end;
      return n;
    }
  }

  char* tempbuf = reqbuf->readbuf + reqbuf->beg;
  n = 1;
  while (reqbuf->beg + n < reqbuf->end) {
    if (tempbuf[n - 1] == '*' && tempbuf[n - 2] == '*' && tempbuf[n - 3] == '*') {
      if (n < reqbuf->end && tempbuf[n] == '*') n++; // au cas ou un byte = 42
      break;
    }
    n++;
  }

  reqbuf->beg += n;
  n = MIN(n, REQ_SIZE); // just to be safe // test this

  if (n > 0)
    memmove(reqbuf->req, tempbuf, n);

  if (n >= REQ_SIZE) {
    if (verbose)
      printf("request too long, truncated\n");
    reqbuf->req[REQ_SIZE - 1] = '*';
    reqbuf->req[REQ_SIZE - 2] = '*';
    reqbuf->req[REQ_SIZE - 3] = '*';
  }

  if (very_verbose) {
    print_incoming_req(reqbuf, n);
  }

  return n;
}

// allows sending of UDP messages
void init_udp_sock() {
  if (udp_sock == -1) {
    udp_sock = socket(PF_INET, SOCK_DGRAM, 0);
    pthread_mutex_init(&udp_sock_mutex, NULL);
  }
}

// send UDP message to player adress
// make sure to call init_udp_sock() before using
// returns 1 on success, 0 on failure
int send_msg_to(player_t* player, char* buf, int len) {
  pthread_mutex_lock(&udp_sock_mutex);
  int n = sendto(udp_sock, buf, len, 0, (struct sockaddr*) &player->addr, sizeof(struct sockaddr_in));
  pthread_mutex_unlock(&udp_sock_mutex);
  if (n == -1) {
    perror("sendto");
    return 0;
  }
  return 1;
}

// multicasts UDP message game multicast adress
// make sure to call init_udp_sock() before using
// returns 1 on success, 0 on failure
int send_msg_multicast(game_t* game, char* buf, int len) {
  pthread_mutex_lock(&udp_sock_mutex);
  int n = sendto(udp_sock, buf, len, 0, (struct sockaddr*) &game->multicast_addr, sizeof(struct sockaddr_in));
  pthread_mutex_unlock(&udp_sock_mutex);
  if (n == -1) {
    perror("sendto");
    return 0;
  }
  return 1;
}
