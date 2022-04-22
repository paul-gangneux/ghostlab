SOURCEFILES := $(shell find src/* | grep .java)
MAINCLASS := launcher.Launcher

client.jar: $(SOURCEFILES)
	javac -d bld -sourcepath src $(SOURCEFILES)
	jar cfe client.jar $(MAINCLASS) -C bld/ .

./PHONY: clean
clean:
	rm -rf client.jar bld/

./PHONY: run
run:
	java -jar client.jar
