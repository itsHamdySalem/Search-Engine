import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDB {
    private MongoCollection<Document> crawlerCollection;

    public void connectToDatabase() {
        try {
            MongoClient mongoClient = new MongoClient();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            crawlerCollection = db.getCollection("crawler");

            System.out.println("Connected to the database");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
