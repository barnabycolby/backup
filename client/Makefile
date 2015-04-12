SRC=src
BIN=bin
JAVAC=javac
JAVA=java
JAR=jar
MAINCLASS=BackupClient
COMMON=../common
JAROUTPUTFILE=client.jar
JARMANIFEST=jarManifest

compile:
	$(JAVAC) $(SRC)/$(MAINCLASS).java -d $(BIN)/ -sourcepath $(COMMON)

run:
	$(JAVA) -classpath $(BIN)/ $(MAINCLASS)

jar:
	$(JAR) cfm $(JAROUTPUTFILE) $(JARMANIFEST) $(BIN)/*.class
