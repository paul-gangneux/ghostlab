#ifndef MAZE_H
#define MAZE_H

#include <sys/types.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h> 
#include "server.h"

// generate and return the mazernth
// sets w and h appropriately
// memory is allocated here, can be 
// freed with a free()
char* maze_generate(u_int16_t* str_w, u_int16_t* str_h);

void maze_print(char* laby, u_int16_t w, u_int16_t h);

#endif