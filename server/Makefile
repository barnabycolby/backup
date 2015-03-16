SRC=src
BIN=bin
JAVAC=javac
JAVA=java
MAINCLASS=BackupServer
COMMON=../common

compile:
	$(JAVAC) $(SRC)/$(MAINCLASS).java -d $(BIN)/ -sourcepath $(SRC):$(COMMON)

run:
	$(JAVA) -classpath $(BIN)/ $(MAINCLASS)
