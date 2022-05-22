#include "maze.h"

#define getCell(i, j) cells[(j) * w + (i)]
#define getRi(i, j) getCell(i+1, j)
#define getLe(i, j) getCell(i-1, j)
#define getUp(i, j) getCell(i, j-1)
#define getDo(i, j) getCell(i, j+1)

typedef struct cell cell;

struct cell {
  int visited;
  cell* riCell;
  cell* doCell;
  cell* upCell;
  cell* leCell;
};

cell* initCells(int w, int h) {
  cell* cells = (cell*) malloc(sizeof(cell) * h * w);
#ifdef DEBUG_FLAG
  debug_nb_malloc_increase_cell();
#endif
  cell* c;

  for (int i = 0; i < w; i++) {
    for (int j = 0; j < h; j++) {
      c = &getCell(i, j);
      c->visited = 0;
      c->riCell = NULL;
      c->leCell = NULL;
      c->upCell = NULL;
      c->doCell = NULL;
      if (i > 0)
        c->leCell = &getLe(i, j);
      if (i < w - 1)
        c->riCell = &getRi(i, j);
      if (j > 0)
        c->upCell = &getUp(i, j);
      if (j < h - 1)
        c->doCell = &getDo(i, j);
    }
  }

  return cells;
}

// returns NULL if all neighbors visited
cell* getRandomNeighbor(cell* c) {
  int n = 0;
  cell* neighbors[4];
  if (c->riCell != NULL && !c->riCell->visited) {
    neighbors[n] = c->riCell;
    n++;
  }
  if (c->leCell != NULL && !c->leCell->visited) {
    neighbors[n] = c->leCell;
    n++;
  }
  if (c->upCell != NULL && !c->upCell->visited) {
    neighbors[n] = c->upCell;
    n++;
  }
  if (c->doCell != NULL && !c->doCell->visited) {
    neighbors[n] = c->doCell;
    n++;
  }
  if (n == 0)
    return NULL;

  return neighbors[random() % n];
}

void destroyWall(cell* c, cell* chosen) {
  if (chosen == c->riCell) {
    c->riCell = NULL;
    chosen->leCell = NULL;
  }
  else if (chosen == c->leCell) {
    c->leCell = NULL;
    chosen->riCell = NULL;
  }
  else if (chosen == c->upCell) {
    c->upCell = NULL;
    chosen->doCell = NULL;
  }
  else if (chosen == c->doCell) {
    c->doCell = NULL;
    chosen->upCell = NULL;
  }
}

void rec_genPath(cell* c) {
  c->visited = 1;
  while (1) {
    cell* c2 = getRandomNeighbor(c);
    if (c2 == NULL)
      return;
    destroyWall(c, c2);;
    rec_genPath(c2);
  }
}

void genPath(cell* cells, int w, int h) {
  int i = random() % w;
  int j = random() % h;
  cell* first = &getCell(i, j);
  rec_genPath(first);
}

void maze_print(char* str, u_int16_t w, u_int16_t h) {
  for (int i = 0; i < w + 2; i++) {
    printf("██");
  }
  printf("\n");
  for (int j = 0; j < h; j++) {
    printf("██");
    for (int i = 0; i < w; i++) {
      if (str[j * w + i] == '1') {
        printf("██");
      }
      else {
        printf("  ");
      }
    }
    printf("██\n");
  }
  for (int i = 0; i < w + 2; i++) {
    printf("██");
  }
  printf("\n\n");
}

// generate and return the maze
// sets w and h appropriately
// memory is allocated here, can be 
// freed with a free()
char* maze_generate(u_int16_t* str_w, u_int16_t* str_h) {
  if (easy_mazes) {
    *str_w = 5;
    *str_h = 7;
    char* easy_lab = (char*) malloc((*str_w) * (*str_h) * sizeof(char));
#ifdef DEBUG_FLAG
    debug_nb_malloc_increase_maze();
#endif
    memset(easy_lab, '0', (*str_w) * (*str_h) * sizeof(char));
    return easy_lab;
  }

  int w = random() % 15 + 5;
  int h = random() % 10 + 5;

  cell* cells = initCells(w, h);
  genPath(cells, w, h);

  u_int16_t sw = w * 2 - 1;
  u_int16_t sh = h * 2 - 1;
  *str_w = sw;
  *str_h = sh;

  char* lab = (char*) malloc(sw * sh * sizeof(char));
#ifdef DEBUG_FLAG
  debug_nb_malloc_increase_maze();
#endif
  memset(lab, '1', sw * sh * sizeof(char));

  for (int i = 0; i < w; i++) {
    for (int j = 0; j < h; j++) {
      lab[(j * 2) * sw + (i * 2)] = '0';
      if (i < (w - 1) && getCell(i, j).riCell == NULL)
        lab[(j * 2) * sw + (i * 2) + 1] = '0';
      if (j < (h - 1) && getCell(i, j).doCell == NULL)
        lab[((j * 2) + 1) * sw + (i * 2)] = '0';
    }
  }

  free(cells);
#ifdef DEBUG_FLAG  
  debug_nb_free_increase_cell();
#endif

  int n = (sw * sh) / 5;
  for (int i = 0; i < n; i++) {
    lab[(random() % sh) * sw + (random() % sw)] = '0';
  }
  if (print_mazes) {
    printf("generated maze:\n");
    maze_print(lab, sw, sh);
  }
  return lab;
}