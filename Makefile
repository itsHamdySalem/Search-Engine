# Makefile

# Crawler module
build-crawler:
	javac -cp ./src/jsoup-1.16.1.jar;./src/mongo-java-driver-3.12.10.jar ./src/Crawler.java ./src/TestCrawler.java ./src/MongoDB.java ./src/RobotObject.java

run-crawler:
	java -cp "./src/jsoup-1.16.1.jar;./src/mongo-java-driver-3.12.10.jar;./src" TestCrawler
	
crawler: build-crawler run-crawler

# Indexer module
build-indexer:
	javac -cp ./src/jsoup-1.16.1.jar;./src/mongo-java-driver-3.12.10.jar ./src/MongoDB.java ./src/Indexer.java

run-indexer:
	java -cp "./src/jsoup-1.16.1.jar;./src/mongo-java-driver-3.12.10.jar;./src" Indexer

indexer: build-indexer run-indexer

# MongoDB module
build-mongodb:
	javac -cp ./src/mongo-java-driver-3.12.10.jar ./src/MongoDB.java

run-mongodb:
	java -cp "./src/mongo-java-driver-3.12.10.jar;./src" MongoDB

mongodb: build-mongodb run-mongodb

# QueryProcessor module
build-queryprocessor:
	javac -cp ./src/snowball-stemmer-1.3.0.581.1.jar ./src/QueryProcessor.java

run-queryprocessor:
	java -p "./src/snowball-stemmer-1.3.0.581.1.jar;./src" QueryProcessor

query-processor: build-queryprocessor run-queryprocessor

clean:
	rm -rf *.class

all: crawler indexer mongodb queryprocessor
