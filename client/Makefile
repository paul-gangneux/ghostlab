SOURCEFILES := $(shell find src/* | grep .java)

./PHONY: client
client: $(SOURCEFILES)
	javac -d bld -sourcepath src $(SOURCEFILES)
