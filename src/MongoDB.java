import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.result.UpdateResult;


import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

import javax.xml.parsers.DocumentBuilder;

import org.bson.Document;
import org.bson.conversions.Bson;


import java.util.HashMap;
import java.util.Map;

import java.util.Set;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;


public class MongoDB {
    private MongoCollection<Document> crawlerCollection;
    private MongoCollection<Document> linksCollection;
    private MongoCollection<Document> indexerCollection;
    private MongoCollection<Document> pagePopularityCollection;

    public void connectToDatabase() {
        try {
            MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            crawlerCollection = db.getCollection("crawler");
            indexerCollection = db.getCollection("indexer");
<<<<<<< HEAD
            pagePopularityCollection = db.getCollection("pagePopularity");
=======
            linksCollection = db.getCollection("links");
>>>>>>> a20449365fe857951f48bc79a6aec733b3cf52c8

            System.out.println("Connected to the database");

            // Create a document to represent the initial state of the crawler
            Document stateDocument = crawlerCollection.find(eq("_id", 1)).first();
            if (stateDocument == null) {
                Document newStateDocument = new Document("_id", 1)
                        .append("state", "idle");
                crawlerCollection.insertOne(newStateDocument);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getState () {
        Document stateDocument = crawlerCollection.find().first();
        Object state = stateDocument.get("state");
        return state.toString();
    }

    public void setState (String state) {
        Bson filter = eq("_id", 1);
        Bson updateState = set("state", state);
        crawlerCollection.updateOne(filter, updateState);
        
        if (state.equals("idle")) {
            // Reset the crawler state
            filter = eq("_id", 1);
            crawlerCollection.updateOne(filter, updateState);
            filter = ne("_id", 1.0);
            crawlerCollection.deleteMany(filter);
        }
    }

    public void getPagesVisited (Queue<String> pagesToVisit, Set<String> pagesVisited) {
        List<Document> pagesList = linksCollection.find().into(new ArrayList<Document>());
        for (Document url: pagesList) {
            if (url.get("_id").toString().equals("1.0")) {
                continue;
            }
            if (url.get("toVisit") != null) pagesToVisit.add(url.get("toVisit").toString());
            else if (url.get("visited") != null) pagesVisited.add(url.get("visited").toString());
        }
    }

    public void updatePagesToVisit (String url, Boolean toInsert) {
        Bson filter = eq("toVisit", url);
        if (toInsert) {
            Document newUrl = new Document("toVisit", url);
            linksCollection.insertOne(newUrl);
        } else {
            linksCollection.deleteOne(filter);
        }
    }

    public void updatePagesVisited (String url) {
        Document newUrl = new Document("visited", url);
        linksCollection.insertOne(newUrl);
    }

    public void uploadIndexer(Map<String, Map<String, Double>> invertedIndex) {
        for (Map.Entry<String, Map<String, Double>> entry : invertedIndex.entrySet()) {
            String word = entry.getKey();
            Map<String, Double> documentScores = entry.getValue();

            // Check if the word already exists in the collection
            Document existingDoc = indexerCollection.find(Filters.eq("word", word)).first();

            if (existingDoc != null) {
                // Word already exists, update its document
                List<Document> docs = (List<Document>) existingDoc.get("docs");

                for (Map.Entry<String, Double> docEntry : documentScores.entrySet()) {
                    String docName = docEntry.getKey();
                    double score = docEntry.getValue();

                    Document doc = new Document("url", docName)
                            .append("score", score);

                    // Append the doc to the "docs" list
                    docs.add(doc);
                }

                // Update the existing document in the collection
                UpdateResult updateResult = indexerCollection.updateOne(
                        Filters.eq("word", word),
                        Updates.addToSet("docs", new Document("$each", docs))
                );

                if (updateResult.getModifiedCount() == 0) {
                    // If the update did not modify any document, it may have exceeded the BSON document size limit
                    // In this case, we can replace the existing document with the updated one
                    indexerCollection.findOneAndReplace(
                            Filters.eq("word", word),
                            existingDoc.append("docs", docs)
                    );
                }
            } else {
                // Word does not exist, insert a new document
                List<Document> docs = new ArrayList<>();

                for (Map.Entry<String, Double> docEntry : documentScores.entrySet()) {
                    String docName = docEntry.getKey();
                    double score = docEntry.getValue();

                    Document doc = new Document("url", docName)
                            .append("score", score);

                    // Append the doc to the "docs" list
                    docs.add(doc);
                }

                // Create the word document
                Document wordDoc = new Document("word", word)
                        .append("docs", docs);

                // Insert the word document into the collection
                indexerCollection.insertOne(wordDoc);
            }
        }

        System.out.println("Upload to MongoDB completed.");
    }
    
    public boolean isPageVisited(String url) {
        Document query = new Document("visited", url);
        long count = linksCollection.countDocuments(query);
        return count > 0;
    }

    public void ConnectWithPagePopularity () {
        try {
            MongoClient mongoClient = new MongoClient();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            pagePopularityCollection = db.getCollection("pagePopularity");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InsertPagePopularity (HashMap<String, Integer> PagesPopularity, Set<String> VisitedPages) {
        for (String page: PagesPopularity.keySet()) {
            if (VisitedPages.contains(page)) {
                Document newPage = new Document("page", page)
                        .append("popularity", PagesPopularity.get(page));
                pagePopularityCollection.insertOne(newPage);
            }
        }
    }

    public HashMap<String, Integer> getPagesPopularity () {
        Set<Document> PagesPopularity = pagePopularityCollection.find().into(new HashSet<Document>());
        HashMap<String, Integer> PagesPopularityMap = new HashMap<String, int>();
        for (Document page: PagesPopularity) {
            PagesPopularityMap.put(page.get("page").toString(), page.get("popularity"));
        }
        return PagesPopularityMap;
    }

}
