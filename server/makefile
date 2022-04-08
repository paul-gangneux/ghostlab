OBJECTS := server game player
OBJECTS := $(addprefix obj/, $(OBJECTS))
OBJECTS := $(addsuffix .o, $(OBJECTS))
CFLAGS := -Wall -Wextra -pedantic -O2
LIBS := -pthread

server: $(OBJECTS)
	gcc $(CFLAGS) $(LIBS) $^ -o $@

obj/%.o: src/%.c include/%.h | obj
	gcc $(CFLAGS) -I ./include -c $< -o $@

obj:
	mkdir obj

clean:
	rm -f obj/* server