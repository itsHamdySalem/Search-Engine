import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;


public class Indexer implements Runnable {
    private static final String DIRECTORY_PATH = "downloadedPages/";

    private int startIndex;
    private int endIndex;
    static MongoDB mongoDBClient = new MongoDB();
    static QueryProcessor qp = new QueryProcessor();


    public Indexer(int startIndex, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        mongoDBClient.connectToDatabase();
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter the number of threads: ");
        String input = reader.readLine();
        int numThreads = Integer.parseInt(input);
        File directory = new File(DIRECTORY_PATH);
        System.out.println(directory.isDirectory());

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                int filesPerThread = files.length / numThreads;
                int startIndex = 0;
                int endIndex = filesPerThread;
                for (int i = 0; i < numThreads - 1; i++) {
                    Thread thread = new Thread(new Indexer(startIndex, endIndex));
                    thread.start();
                    startIndex = endIndex;
                    endIndex += filesPerThread;
                }
                // Start the last thread with the remaining files
                Thread thread = new Thread(new Indexer(startIndex, files.length));
                thread.start();
            }
        }
    }

    @Override
    public void run() {
        File directory = new File(DIRECTORY_PATH);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (int i = startIndex; i < endIndex; i++) {
                    File file = files[i];
                    if (file.isFile()) {
                        collectDocument(file);
                    }
                    file.delete();
                }
            }
        }
    }

    private static void collectDocument(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder document = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                document.append(line).append("\n");
            }
            // Process the collected document as needed
            System.out.println("File Name: " + file);
            parseHTML(document.toString(), file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     private static void parseHTML(String htmlContent, String fileName) {
        Document docc = Jsoup.parse(htmlContent);
        Map<String, Map<String, Double>> invertedIndex = new HashMap<>();
        scoreWords(docc.body(), invertedIndex, 1.0, fileName);
        printInvertedIndex(invertedIndex);
        uploadToDB(invertedIndex);
    }

    private static void scoreWords(Element element, Map<String, Map<String, Double>> invertedIndex, double currentScore, String docName) {
        if (element == null) {
            return;
        }

        for (Element child : element.children()) {
            scoreWords(child, invertedIndex, currentScore * getElementScore(child), docName);
        }

        String text = element.ownText().trim();
        if (!text.isEmpty()) {
            double score = currentScore * getElementScore(element);
            String[] words = text.split("\\s+");
            for (String word : words) {
                word = qp.ProcessQuery(word);
                invertedIndex.computeIfAbsent(word, k -> new HashMap<>()).merge(docName, score, Double::sum);
            }
        }
    }

    private static double getElementScore(Element element) {
        String tagName = element.tagName();
        double score = 0;

        if (tagName.equals("title")) {
            score = 1.0;
        } else if (tagName.equals("h1")) {
            score = 0.9;
        } else if (tagName.equals("h2")) {
            score = 0.8;
        } else if (tagName.equals("h3")) {
            score = 0.7;
        } else if (tagName.equals("h4")) {
            score = 0.6;
        } else if (tagName.equals("h5")) {
            score = 0.5;
        } else if (tagName.equals("h6")) {
            score = 0.4;
        } else if (tagName.equals("p")) {
            score = 0.3;
        } else if (tagName.equals("a")) {
            score = 0.5;
        } else if (tagName.equals("ul") || tagName.equals("ol")) {
            score = 1.0;
        } else if (tagName.equals("li")) {
            score = 0.3;
        } else if (tagName.equals("div")) {
            score = 1.0;
        } else if (tagName.equals("span")) {
            score = 0.3;
        } else if (tagName.equals("br")) {
            score = 1.0;
        } else if (tagName.equals("table") || tagName.equals("tr") || tagName.equals("td")) {
            score = 1.0;
        } else if (tagName.equals("form")) {
            score = 1.0;
        } else if (tagName.equals("input") || tagName.equals("button")) {
            score = 0.3;
        } else if (tagName.equals("header") || tagName.equals("footer") || tagName.equals("nav") || tagName.equals("main") || tagName.equals("section")) {
            score = 1.0;
        }

        return score;
    }


    private static void printInvertedIndex(Map<String, Map<String, Double>> invertedIndex) {
        for (Map.Entry<String, Map<String, Double>> entry : invertedIndex.entrySet()) {
            String word = entry.getKey();
            Map<String, Double> documentScores = entry.getValue();
            System.out.println("Word: " + word);
            for (Map.Entry<String, Double> docEntry : documentScores.entrySet()) {
                String docName = docEntry.getKey();
                double score = docEntry.getValue();
                System.out.println("\tDocName: " + docName + ", Score: " + score);
            }
        }
    }

    public static void uploadToDB(Map<String, Map<String, Double>> invertedIndex) {
        mongoDBClient.uploadIndexer(invertedIndex);
        System.out.println("Upload to MongoDB completed.");
    }


}


