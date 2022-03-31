OBJECTS := server game
OBJECTS := $(addprefix obj/, $(OBJECTS))
OBJECTS := $(addsuffix .o, $(OBJECTS))
CFLAGS := -Wall -Wextra -pedantic -O2
LIBS := -pthread

server: $(OBJECTS)
	gcc $(CFLAGS) $(LIBS) $< -o $@

obj/%.o: src/%.c include/%.h
	gcc $(CFLAGS) -I ./include -c $< -o $@

clean:
	rm -f obj/* server