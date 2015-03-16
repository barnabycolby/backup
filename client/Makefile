SRC=src
BIN=bin
JAVAC=javac
JAVA=java
MAINCLASS=BackupClient
COMMON=../common

compile:
	$(JAVAC) $(SRC)/$(MAINCLASS).java -d $(BIN)/ -sourcepath $(COMMON)

run:
	$(JAVA) -classpath $(BIN)/ $(MAINCLASS)
