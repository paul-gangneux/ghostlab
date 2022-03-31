#include "game.h"
//TODO: changer les types

typedef struct game {
  u_int8_t id;
  u_int16_t w,h;
  u_int8_t nbOfPlayers;
  u_int8_t nbOfGhosts;
  char multicast_port[4];

  int* labyrinth;
} game;

// ce sera sûrement la même tout le temps, c'est donc une variable partagée
const char multicast_ip[15];