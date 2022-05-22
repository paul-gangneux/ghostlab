#ifndef DEBUG_H
#define DEBUG_H

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

void debug_nb_malloc_increase();
void debug_nb_free_increase();

void debug_nb_malloc_increase_game();
void debug_nb_free_increase_game();

void debug_nb_malloc_increase_maze();
void debug_nb_free_increase_maze();

void debug_nb_malloc_increase_ghost();
void debug_nb_free_increase_ghost();

void debug_nb_malloc_increase_gamelist();
void debug_nb_free_increase_gamelist();

void debug_nb_malloc_increase_gamecell();
void debug_nb_free_increase_gamecell();

void debug_nb_malloc_increase_cell();
void debug_nb_free_increase_cell();

void debug_nb_malloc_increase_player();
void debug_nb_free_increase_player();

void debug_nb_malloc_increase_playerlist();
void debug_nb_free_increase_playerlist();

void debug_nb_malloc_increase_playercell();
void debug_nb_free_increase_playercell();

void debug_nb_malloc_increase_mallbuffer();
void debug_nb_free_increase_mallbuffer();

void debug_print_memory_usage();

#endif
