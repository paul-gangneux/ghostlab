CFLAGS = -Wall -pedantic -O2
OBJECTS := obj/server.o obj/game.o

server: $(OBJECTS)
	gcc $(CFLAGS) -pthread $< -o $@

obj/%.o: src/%.c include/%.h
	gcc $(CFLAGS) -c $< -o $@

clean:
	rm -f obj/* server