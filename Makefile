# Makefile

# Crawler module
build-crawler:
	javac -cp ./src/jsoup-1.16.1.jar;./src/mongo-java-driver-3.12.10.jar ./src/Crawler.java ./src/TestCrawler.java ./src/MongoDB.java ./src/RobotObject.java

run-crawler:
	java -cp "./src/jsoup-1.16.1.jar;./src/mongo-java-driver-3.12.10.jar;./src" TestCrawler
	
crawler: build-crawler run-crawler

# Indexer module
build-indexer:
	javac -cp ./src/lucene-analyzers-common-8.8.2.jar;./src/jsoup-1.16.1.jar;./src/mongo-java-driver-3.12.10.jar ./src/QueryProcessor.java ./src/MongoDB.java ./src/Indexer.java

run-indexer:
	java -cp "./src/lucene-analyzers-common-8.8.2.jar;./src/jsoup-1.16.1.jar;./src/mongo-java-driver-3.12.10.jar;./src" Indexer

indexer: build-indexer run-indexer

# MongoDB module
build-mongodb:
	javac -cp ./src/mongo-java-driver-3.12.10.jar ./src/MongoDB.java

run-mongodb:
	java -cp "./src/mongo-java-driver-3.12.10.jar;./src" MongoDB

mongodb: build-mongodb run-mongodb

# QueryProcessor module
build-queryprocessor:
	javac -cp "./src/lucene-analyzers-common-8.8.2.jar" ./src/QueryProcessor.java

run-queryprocessor:
	java -cp "./src;./src/lucene-analyzers-common-8.8.2.jar" QueryProcessor

query-processor: build-queryprocessor run-queryprocessor

crawler-indexer: crawler indexer

clean:
	rm -rf *.class

all: crawler indexer mongodb queryprocessor
