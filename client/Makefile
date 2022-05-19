SOURCEFILES := $(shell find src/* | grep .java)
RESOURCES := $(shell find resources/*)
MAINCLASS := launcher.Launcher

../client.jar: $(SOURCEFILES) $(RESOURCES) | bld
	javac -d bld -sourcepath src $(SOURCEFILES)
	jar cfe $@ $(MAINCLASS) $(RESOURCES) -C bld/ .
	chmod u+x ../client.jar 

bld:
	mkdir bld

./PHONY: clean
clean:
	rm -rf ../client.jar bld/*
