SRC=src
BIN=bin
JAVAC=javac
JAVA=java
JAR=jar
MAINCLASS=BackupServer
COMMON=../common
JAROUTPUTFILE=server.jar
JARMANIFEST=jarManifest

compile:
	$(JAVAC) $(SRC)/$(MAINCLASS).java -d $(BIN)/ -sourcepath $(SRC):$(COMMON)

run:
	$(JAVA) -classpath $(BIN)/ $(MAINCLASS)

jar:
	$(JAR) cfm $(JAROUTPUTFILE) $(JARMANIFEST) $(BIN)/*.class
