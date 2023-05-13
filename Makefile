# Makefile

# Crawler module
crawler:
	javac -cp ./src/jsoup-1.16.1.jar ./src/Crawler.java

run-crawler:
	java -cp ./src/jsoup-1.16.1.jar ./src/Crawler.java

# Indexer module
indexer:
	javac ./Indexer.java

run-indexer:
	java ./Indexer.class

# MongoDB module
mongodb:
	javac -cp ./src/mongo-java-driver-3.12.10.jar ./src/MongoDB.java

run-mongodb:
	java -cp ./src/mongo-java-driver-3.12.10.jar ./src/MongoDB.class

# Compile and run all modules
all: crawler indexer mongodb
	java -cp ./src/jsoup-1.16.1.jar:mongo-java-driver-3.12.8.jar Test

clean:
	rm -rf *.class

all: crawler run-crawler indexer run-indexer mongodb run-mongodb
