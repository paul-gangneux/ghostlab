SOURCEFILES := $(shell find src/* | grep .java)
MAINCLASS := launcher.Launcher

../client.jar: $(SOURCEFILES) | bld
	javac -d bld -sourcepath src $(SOURCEFILES)
	jar cfe $@ $(MAINCLASS) -C bld/ .

bld:
	mkdir bld

./PHONY: clean
clean:
	rm -rf ../client.jar bld/*

./PHONY: run
run:
	java -jar client.jar