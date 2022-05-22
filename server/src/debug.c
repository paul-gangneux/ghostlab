#include "debug.h"

pthread_mutex_t debug_lock = PTHREAD_MUTEX_INITIALIZER;

int nb_alloc = 0;
int nb_free = 0;

int nb_alloc_game = 0;
int nb_free_game = 0;

int nb_alloc_gamecell = 0;
int nb_free_gamecell = 0;

int nb_alloc_gamelist = 0;
int nb_free_gamelist = 0;

int nb_alloc_player = 0;
int nb_free_player = 0;

int nb_alloc_playercell = 0;
int nb_free_playercell = 0;

int nb_alloc_playerlist = 0;
int nb_free_playerlist = 0;

int nb_alloc_maze = 0;
int nb_free_maze = 0;

int nb_alloc_ghost = 0;
int nb_free_ghost = 0;

int nb_alloc_cell = 0;
int nb_free_cell = 0;

int nb_alloc_mallbuffer = 0;
int nb_free_mallbuffer = 0;

void debug_nb_malloc_increase_game() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_game++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_free_increase_game() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_game++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_maze() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_maze++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_maze() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_maze++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_ghost() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_ghost++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_ghost() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_ghost++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_gamelist() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_gamelist++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_gamelist() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_gamelist++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_gamecell() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_gamecell++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_gamecell() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_gamecell++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_cell() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_cell++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_cell() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_cell++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_player() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_player++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_player() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_player++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_playerlist() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_playerlist++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_playerlist() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_playerlist++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_playercell() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_playercell++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_playercell() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_playercell++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_nb_malloc_increase_mallbuffer() {
  pthread_mutex_lock(&debug_lock);
  nb_alloc++;
  nb_alloc_mallbuffer++;
  pthread_mutex_unlock(&debug_lock);
}
void debug_nb_free_increase_mallbuffer() {
  pthread_mutex_lock(&debug_lock);
  nb_free++;
  nb_free_mallbuffer++;
  pthread_mutex_unlock(&debug_lock);
}

void debug_print_memory_usage() {
  pthread_mutex_lock(&debug_lock);
  printf(
    "Memory usage:\n"
    "  total:      %3d allocs, %3d frees\n"
    "  game:       %3d allocs, %3d frees\n"
    "  gameCell:   %3d allocs, %3d frees\n"
    "  gameList:   %3d allocs, %3d frees\n"
    "  player:     %3d allocs, %3d frees\n"
    "  playerCell: %3d allocs, %3d frees\n"
    "  playerList: %3d allocs, %3d frees\n"
    "  maze:       %3d allocs, %3d frees\n"
    "  ghost:      %3d allocs, %3d frees\n"
    "  cell:       %3d allocs, %3d frees\n"
    "  mallbuf:    %3d allocs, %3d frees\n\n",
    nb_alloc,
    nb_free,

    nb_alloc_game,
    nb_free_game,

    nb_alloc_gamecell,
    nb_free_gamecell,

    nb_alloc_gamelist,
    nb_free_gamelist,

    nb_alloc_player,
    nb_free_player,

    nb_alloc_playercell,
    nb_free_playercell,

    nb_alloc_playerlist,
    nb_free_playerlist,

    nb_alloc_maze,
    nb_free_maze,

    nb_alloc_ghost,
    nb_free_ghost,

    nb_alloc_cell,
    nb_free_cell,

    nb_alloc_mallbuffer,
    nb_free_mallbuffer
  );
  pthread_mutex_unlock(&debug_lock);
}
