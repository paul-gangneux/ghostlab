SOURCEFILES := $(shell find src/* | grep .java)
MAINCLASS := launcher.Launcher

../client.jar: $(SOURCEFILES) | bld
	javac -d bld -sourcepath src $(SOURCEFILES)
	jar cfe $@ $(MAINCLASS) -C bld/ .
	chmod u+x ../client.jar 

bld:
	mkdir bld

./PHONY: clean
clean:
	rm -rf ../client.jar bld/*
