
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;


import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

import javax.xml.parsers.DocumentBuilder;

import org.bson.Document;
import org.bson.conversions.Bson;


import java.util.Set;
import java.util.List;
import java.util.Queue;
import java.util.ArrayList;


public class MongoDB {
    private MongoCollection<Document> crawlerCollection;

    public void connectToDatabase() {
        try {
            MongoClient mongoClient = new MongoClient();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            crawlerCollection = db.getCollection("crawler");

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
        List<Document> pagesList = crawlerCollection.find().into(new ArrayList<Document>());
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
            crawlerCollection.insertOne(newUrl);
        } else {
            crawlerCollection.deleteOne(filter);
        }
    }

    public void updatePagesToVisit (List<String> urls) {
        for (String url: urls) {
            Document newUrl = new Document("toVisit", url);
            crawlerCollection.insertOne(newUrl);
        }
    }

    public void updatePagesVisited (String url) {
        Document newUrl = new Document("visited", url);
        crawlerCollection.insertOne(newUrl);
    }

}




