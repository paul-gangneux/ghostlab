OBJECTS := server game player communication maze
DEBUG_OBJECTS := debug
DEBUG_OBJECTS += $(OBJECTS)

OBJECTS := $(addprefix obj/, $(OBJECTS))
OBJECTS := $(addsuffix .o, $(OBJECTS))

DEBUG_OBJECTS := $(addprefix obj-debug/, $(DEBUG_OBJECTS))
DEBUG_OBJECTS := $(addsuffix .o, $(DEBUG_OBJECTS))

CFLAGS := -Wall -Wextra -pedantic -O2
DEBUG_FLAGS := -Wall -Wextra -pedantic -O0 -g -D DEBUG_FLAG
LIBS := -pthread

../server.out: $(OBJECTS)
	gcc $(CFLAGS) $(LIBS) $^ -o $@

../server_debug.out: $(DEBUG_OBJECTS)
	gcc $(DEBUG_FLAGS) $(LIBS) $^ -o $@

obj/%.o: src/%.c include/%.h | obj
	gcc $(CFLAGS) -I ./include -c $< -o $@

obj-debug/%.o: src/%.c include/%.h | obj-debug
	gcc $(DEBUG_FLAGS) -I ./include -c $< -o $@

obj:
	mkdir obj

obj-debug:
	mkdir obj-debug

clean:
	rm -f obj/* obj-debug/* ../server.out ../server_debug.out

./PHONY: debug
debug: ../server_debug.out