#include "game.h"

#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>

#define lock(x) pthread_mutex_lock(&x->mutex)
#define unlock(x) pthread_mutex_unlock(&x->mutex)

#define in_a_wall(game, thing)\
  game->maze[thing->x + thing->y * game->w] == '1'

#define in_a_wall2(game, thing)\
  game->maze[thing.x + thing.y * game->w] == '1'

typedef struct gameCell gameCell_t;

struct gameCell {
  game_t* game;
  gameCell_t* next;
};

struct gameList {
  pthread_mutex_t mutex;
  u_int8_t length;
  gameCell_t* first;
};

struct ghost {
  int x, y;
};

void randomise_ghosts_pos(game_t* game, int informPlayers) {
  char buf[16];
  memmove(buf, "GHOST xxx yyy+++", 16);

  lock(game);
  for (int i = 0; i < game->nb_ghosts; i++) {
    do {
      game->ghosts[i].x = random() % game->w;
      game->ghosts[i].y = random() % game->h;
    } while (in_a_wall2(game, game->ghosts[i]));
    if (informPlayers) {
      mv_num3toBuf(buf, 6, game->ghosts[i].x);
      mv_num3toBuf(buf, 10, game->ghosts[i].y);
      // sends [GHOST x y+++]
      send_msg_multicast(game, buf, 16);
    }
  }
  unlock(game);
}

// allocate memory for a game. free with freeGame()
// does not set id and port to their correct values
game_t* newGame() {
  game_t* g = (game_t*) malloc(sizeof(game_t));
  int n = pipe(g->pipe1);
  if (n < 0) {
    perror("pipe");
    free(g);
    return NULL;
  }
  n = pipe(g->pipe2);
  if (n < 0) {
    perror("pipe");
    close(g->pipe1[0]);
    close(g->pipe1[1]);
    free(g);
    return NULL;
  }
  pthread_mutex_init(&g->mutex, NULL);
  g->id = 0;
  g->nb_players = 0;
  g->nb_ghosts = random() % 5 + 3;
  g->multicast_port[0] = '4';
  g->multicast_port[1] = '0';
  g->multicast_port[2] = '0';
  g->multicast_port[3] = '0';
  g->hasStarted = 0;
  g->maze = maze_generate(&g->w, &g->h);
  g->playerList = newPlayerList();
  g->ghosts = (ghost_t*) malloc(sizeof(ghost_t) * g->nb_ghosts);
  randomise_ghosts_pos(g, 0);
  memset(&g->multicast_addr, 0, sizeof(g->multicast_addr));
  return g;
}

// allocate memory for a game list. free with freeGameList()
gameList_t* newGameList() {
  gameList_t* gl = (gameList_t*) malloc(sizeof(gameList_t));
  pthread_mutex_init(&gl->mutex, NULL);
  gl->length = 0;
  gl->first = NULL;
  return gl;
}

// allocate memory for a game cell. free with freeGameCell()
gameCell_t* newGameCell(game_t* g) {
  gameCell_t* gc = (gameCell_t*) malloc(sizeof(gameCell_t));
  gc->game = g;
  gc->next = NULL;
  return gc;
}

void freeGame(game_t* game) {
  lock(game);
  freePlayerList(game->playerList);
  unlock(game);
  if (game->hasStarted) {
    int n = write(game->pipe1[1], "end", 3);
    if (n > 0) {
      char buf[3];
      // on attend la fin du thread de jeu
      n = read(game->pipe2[0], buf, 3);
      if (n < 0) {
        perror("read from pipe");
      }
      else if (verbose) {
        printf("game thread ended\n");
      }
    }
    else {
      perror("write in pipe");
    }
  }
  pthread_mutex_destroy(&game->mutex);
  free(game->maze);
  free(game->ghosts);
  close(game->pipe1[0]);
  close(game->pipe1[1]);
  close(game->pipe2[0]);
  close(game->pipe2[1]);
  if (verbose) {
    printf("game %d deleted\n", game->id);
  }
  free(game);
  game = NULL;
}

void freeGameCell(gameCell_t* gameCell) {
  if (gameCell->next != NULL)
    freeGameCell(gameCell->next);
  freeGame(gameCell->game);
  free(gameCell);
  gameCell = NULL;
}

void freeGameList(gameList_t* gameList) {
  pthread_mutex_destroy(&gameList->mutex);
  if (gameList->first != NULL) {
    freeGameCell(gameList->first);
  }
  free(gameList);
  gameList = NULL;
}

int insertIntoList(gameCell_t* curr, gameCell_t* gc, u_int8_t n) {
  if (curr->next == NULL || n < curr->next->game->id) {
    gc->next = curr->next;
    curr->next = gc;
    gc->game->id = n;
    // kinda dirty but works
    gc->game->multicast_port[1] += n / 100;
    gc->game->multicast_port[2] += (n % 100) / 10;
    gc->game->multicast_port[3] += (n % 10);
    return n;
  }
  return insertIntoList(curr->next, gc, n + 1);
}

void init_multicast_addr(game_t* game) {
  char strport[5];
  strport[4] = '\0';
  memmove(strport, game->multicast_port, 4);
  int port = atoi(strport);
  game->multicast_addr.sin_port = htons(port);
  game->multicast_addr.sin_family = AF_INET;
  inet_aton(multicast_ip_address, &game->multicast_addr.sin_addr);
}

// add game to the game list, returns game id on success, -1 on failure.
// sets game->id and game->multicast_port appropriately.
// initiates the multicast socket
int gameList_add(gameList_t* gl, game_t* g) {
  lock(gl);
  if (gl->length == 0xff) {
    unlock(gl);
    return -1;
  }
  gameCell_t* gc = newGameCell(g);
  int x;

  if (gl->first == NULL || gl->first->game->id > 1) {
    gc->next = gl->first;
    gc->game->id = 1;
    gc->game->multicast_port[3] = '1';
    gl->length += 1;
    gl->first = gc;
    x = 1;
  }

  else {
    x = insertIntoList(gl->first, gc, 2);
    gl->length += 1;
  }
  init_multicast_addr(g);
  unlock(gl);
  return x;
}

u_int8_t get_nb_of_started_games_aux(gameCell_t* cell, u_int8_t i) {
  if (cell == NULL)
    return i;
  if (cell->game->hasStarted) {
    return get_nb_of_started_games_aux(cell->next, i + 1);
  }
  return get_nb_of_started_games_aux(cell->next, i);
}

u_int8_t get_nb_of_started_games(gameList_t* gameList) {
  if (gameList == NULL)
    return 0;
  return get_nb_of_started_games_aux(gameList->first, 0);
}

#define NB_MAX 10
#define OGAME_LEN 12

// send the [GAMES n***] and [OGAME id_game nb_players***] messages to client.
// only send games that haven't started yet
// returns 0 on success, -1 on error.
int gameList_sendToCli(gameList_t* gameList, int cli_fd) {
  lock(gameList);
  int i;
  char buf[OGAME_LEN * NB_MAX];
  memmove(buf, "GAMES 0***", 10);
  buf[6] = gameList->length - get_nb_of_started_games(gameList);
  send_msg(cli_fd, buf, 10);

  gameCell_t* gc = gameList->first;
  for (i = 0; i < NB_MAX; i++)
    memmove(buf + i * OGAME_LEN, "OGAME 0 0***", OGAME_LEN);
  i = 0;
  while (gc != NULL) {
    if (!gc->game->hasStarted) {
      buf[i * OGAME_LEN + 6] = gc->game->id;
      buf[i * OGAME_LEN + 8] = gc->game->nb_players;
      i++;
    }
    gc = gc->next;
    if (i == NB_MAX) {
      if (!send_msg(cli_fd, buf, OGAME_LEN * NB_MAX))
        goto error;
      i = 0;
    }
  }

  if (i > 0) {
    if (!send_msg(cli_fd, buf, OGAME_LEN * i))
      goto error;
  }

  unlock(gameList);
  return 0;

  error:

  unlock(gameList);
  return -1;
}

game_t* aux_game_get(gameCell_t* gc, u_int8_t id) {
  if (gc == NULL)
    return NULL;
  if (gc->game->id == id)
    return gc->game;
  return aux_game_get(gc->next, id);
}

// returns NULL on failure
game_t* game_get(gameList_t* gameList, u_int8_t id) {
  lock(gameList);
  game_t* game = aux_game_get(gameList->first, id);
  unlock(gameList);
  return game;
}

// returns -1 on failure, 0 on success
// will fail if there's another user with the same name in that game
int game_addPlayer(gameList_t* gameList, u_int8_t game_id, player_t* player) {
  lock(gameList);
  game_t* game = aux_game_get(gameList->first, game_id);
  if (game == NULL) {
    unlock(gameList);
    return -1;
  }
  int i = 0;
  lock(game);
  if (game->hasStarted || playerList_hasPlayerWithSameId(game->playerList, player))
    i = -1;
  else {
    player_addToList(game->playerList, player);
    game->nb_players++;
  }
  unlock(game);
  unlock(gameList);
  return i;
}

// remove player from game and asks for his thread to end
void game_removePlayer(game_t* game, player_t* player) {
  lock(game);
  if (playerList_remove(game->playerList, player))
    game->nb_players--;
  unlock(game);
}

// returns 0 on failure, 1 on success
int gameList_remove_aux(gameCell_t* gc, game_t* game) {
  if (gc->next == NULL)
    return 0;
  if (gc->next->game == game) {
    gameCell_t* temp = gc->next;
    gc->next = gc->next->next;
    temp->next = NULL;
    freeGameCell(temp);
    return 1;
  }
  return gameList_remove_aux(gc->next, game);
}

// removes game from gameList and free memory
// use with option RM_NOPLAYERS to only remove if no players are in the game.
// use with option RM_FORCE to remove no matter what
// will free associated playerlist and ask player to disconnect
void gameList_remove(gameList_t* gameList, game_t* game, int option) {
  lock(gameList);
  if (option == RM_NOPLAYERS) {
    if (game->nb_players > 0) {
      unlock(gameList);
      return;
    }
  }
  if (gameList->first == NULL) {
    unlock(gameList);
    return;
  }
  if (gameList->first->game == game) {
    gameCell_t* gc = gameList->first;
    gameList->first = gameList->first->next;
    gc->next = NULL;
    freeGameCell(gc);
    gameList->length -= 1;
    unlock(gameList);
    return;
  }
  if (gameList_remove_aux(gameList->first, game))
    gameList->length -= 1;
  unlock(gameList);
}

// send the [LIST! m s***] and [PLAYR id***] messages to client.
// returns -1 on failure, 0 on success
int game_sendPlayerList(gameList_t* gameList, u_int8_t game_id, int cli_fd) {
  game_t* game = game_get(gameList, game_id);
  if (game == NULL)
    return -1;
  lock(game);
  int n = playerList_sendToCli(game->playerList, game_id, cli_fd);
  unlock(game);
  return n;
}

void* gameThread(void* arg) {
  game_t* game = (game_t*) arg;

  struct pollfd pollfd = { .fd = game->pipe1[0], .events = POLLIN };

  while (1) {
    poll(&pollfd, 1, 1000);

    if (pollfd.revents & POLLIN) {
      char buf[3] = { 0, 0, 0 };
      int n = read(game->pipe1[0], buf, 3);
      if (n == 3 && cmp3chars(buf, "end"))
        goto end;
    }

    randomise_ghosts_pos(game, 1);
  }

  end:

  if (write(game->pipe2[1], "yes", 3) < 0) {
    perror("gameThread: write");
  }
  return NULL;
}

void send_begin_message(player_t* player) {
  int n = write(player->pipe[1], "bgn", 3);
  if (n < 0) {
    perror("send_begin_message");
  }
}

// starts game in a new thread if all players are ready.
// demands that at least one player is registered
void game_startIfAllReady(game_t* game) {
  lock(game);
  int ready = playerList_allReady(game->playerList);
  if (game->hasStarted) {
    ready = 0; // to avoid launching a game twice
  }

  if (ready) {
    game->hasStarted = 1;
    if (verbose) {
      printf("game %d: all players ready\n", game->id);
    }
    pthread_t thread;
    pthread_create(&thread, NULL, gameThread, (void*) game);
    playerList_forAll(game->playerList, send_begin_message);
  }
  unlock(game);
}

// out of bounds
#define out_of_bounds(game, thing)\
  thing->x < 0 || thing->x >= game->w ||\
  thing->y < 0 || thing->y >= game->h

// returns 1 if the player captured at least one ghost, else returns 0
int game_movePlayer(game_t* game, player_t* player, int amount, int direction) {
  int capturedAGhost = 0;
  int delta = 0;
  int* axis = NULL;

  switch (direction) {
    case MV_UP:
      delta = -1;
      axis = &player->y;
      break;
    case MV_DOWN:
      delta = 1;
      axis = &player->y;
      break;
    case MV_LEFT:
      delta = -1;
      axis = &player->x;
      break;
    case MV_RIGHT:
      delta = 1;
      axis = &player->x;
      break;
    default:
      return 0;
  }

  while (amount > 0) {
    *axis += delta;
    if (out_of_bounds(game, player) || in_a_wall(game, player)) {
      *axis -= delta;
      break;
    }

    char buf[30];
    memmove(buf, "SCORE username pppp xxx yyy+++", 30);
    memmove(buf + 6, player->name, MAX_NAME);

    lock(game);
    for (int i = 0; i < game->nb_ghosts; i++) {
      if (game->ghosts[i].x == player->x && game->ghosts[i].y == player->y) {
        game->nb_ghosts--;
        game->ghosts[i] = game->ghosts[game->nb_ghosts];
        capturedAGhost = 1;
        player->score++;

        mv_num4toBuf(buf, 15, player->score);
        mv_num3toBuf(buf, 20, player->x);
        mv_num3toBuf(buf, 24, player->y);
        send_msg_multicast(game, buf, 30);
      }
    }
    unlock(game);
    amount--;
  }
  return capturedAGhost;
}

game_t* game_getSize(gameList_t* gameList, u_int8_t id_game, u_int16_t* h, u_int16_t* w) {
  lock(gameList);
  game_t* game = aux_game_get(gameList->first, id_game);
  if (game == NULL) {
    unlock(gameList);
    return NULL;
  }
  *h = game->h;
  *w = game->w;
  unlock(gameList);
  return game;
}

int in_a_ghost(game_t* game, player_t* player) {
  for (int i = 0; i < game->nb_ghosts; i++) {
    if (game->ghosts[i].x == player->x && game->ghosts[i].y == player->y)
      return 1;
  }
  return 0;
}

int in_another_player(game_t* game, player_t* player) {
  return playerList_inAnotherPlayer(game->playerList, player);
}

void game_randomizePosition(game_t* game, player_t* player) {
  int i = 0;
  lock(game);
  do {
    player->x = random() % game->w;
    player->y = random() % game->h;
    i++;
  } while (
    (
      in_a_wall(game, player) ||
      in_a_ghost(game, player) ||
      in_another_player(game, player))
    && i < 10000);
  unlock(game);

  if (i == 10000) {
    printf("problem with game_randomizePosition function");
    int n = write(player->pipe[1], "end", 3);
    if (n < 0) {
      perror("send_begin_message");
    }
  }
}

// sends [GLIS! s***] and [GPLYR username x y p***]
// returns 1 on success, 0 on failure
int game_sendPlayerList_AllInfos(game_t* game, int cli_fd) {
  lock(game);
  int n = playerList_sendToCli_AllInfos(game->playerList, cli_fd);
  unlock(game);
  return n;
}

// send n bytes from buf to dest user via UDP
// returns 1 on success, 0 on failure
int game_sendMessageToOnePlayer(game_t* game, char destId[8], char* buf, int len) {
  int ret = 0;
  lock(game);
  player_t* destPlayer = playerList_getPlayer(game->playerList, destId);
  if (destPlayer != NULL) {
    ret = send_msg_to(destPlayer, buf, len);
  }
  unlock(game);
  return ret;
}

// for when game ended properly
void send_end_message(player_t* player) {
  int n = write(player->pipe[1], "fin", 3);
  if (n < 0) {
    perror("send_end_message");
  }
}

void game_endIfNoGhost(game_t* game) {
  lock(game);
  if (game->nb_ghosts == 0) {
    playerList_forAll(game->playerList, send_end_message);
  }
  player_t* winner = playerList_getPlayerWithMaxScore(game->playerList);
  if (winner != NULL) {
    char buf[22];
    memmove(buf, "ENDGA username pppp+++", 22);
    memmove(buf + 6, winner->name, MAX_NAME);
    mv_num4toBuf(buf, 15, winner->score);
    // sends [ENDGA id p+++]
    send_msg_multicast(game, buf, 22);
  }
  unlock(game);
}