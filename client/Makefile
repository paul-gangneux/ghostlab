client:
	javac src/*.java

clean:
	rm -f src/*.class
	rm -f src/launcher/*.class
	rm -f src/model/*.class
	rm -f src/ui/*.class
	rm -f src/ui/panels/game/*.class
	rm -f src/ui/panels/launcher/*.class
	rm -f src/ui/panels/lobby/*.class
	rm -f src/ui_tests/*.class

run:
	java Client

ui:
	javac src/launcher/*.java src/model/*.java src/ui/*.java src/ui_tests/*.java

default: clean client run