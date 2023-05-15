package main.java;
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
import org.tartarus.snowball.ext.PorterStemmer;

public class Indexer implements Runnable {
    String[] stopWords = {"a", "an", "the", "is", "are", "am", "was", "were", "has", "have", "had", "been", "will", "shall", "be", "do", "does", "did", "can", "could", "may", "might", "must", "should", "of", "in", "on", "at", "to", "from", "by", "for", "about", "with", "without", "not", "no", "yes", "or", "and", "but", "if", "else", "then", "than", "else", "when", "where", "what", "who", "how", "which", "whom", "whose", "why", "because", "however", "therefore", "thus", "so", "such", "this", "that", "these", "those", "their", "his", "her", "its", "our", "your", "their", "any", "some", "many", "much", "few", "little", "own", "other", "another", "each", "every", "all", "both", "neither", "either", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth"};
    String[] punctuations = {".", ",", ":", ";", "?", "!", "'", "\"", "(", ")", "{", "}", "[", "]", "<", ">", "/", "\\", "|", "-", "_", "+", "=", "*", "&", "^", "%", "$", "#", "@", "`", "~", "“", "”", "‘", "’", "–", "—", "…"};


    private static final String DIRECTORY_PATH = "downloadedPages/";

    private int startIndex;
    private int endIndex;
    static MongoDB mongoDBClient = new MongoDB();
    static int document_numbers ;

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
                document_numbers= files.length;
                int filesPerThread  = files.length / numThreads;
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
        Map<String, Map<String, word_par>> invertedIndex = new HashMap<>();
        // the doc is an html content we need to pre process it
        scoreWords(docc.body(), invertedIndex, 1.0, fileName);
        printInvertedIndex(invertedIndex);
        uploadToDB(invertedIndex);
    }

    private static void scoreWords(Element element, Map<String, Map<String, word_par>> invertedIndex, double currentScore, String docName) {
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

                //stemming ,phrase,tolower
                invertedIndex.computeIfAbsent(word, k -> new HashMap<>()). merge(docName, score, Double::sum);
            }
        }
    }

    private static double getElementScore(Element element) {
        String tagName = element.tagName();
        double score = 1.0; // Default score

        if (tagName.equals("title")) {
            score = 1.0; // Assign a score of 1.0 to title elements
        } else if (tagName.equals("h1")) {
            score = 0.9; // Assign a score of 0.9 to h1 elements
        } else if (tagName.equals("h2")) {
            score = 0.8; // Assign a score of 0.8 to h2 elements
        } else if (tagName.equals("p")) {
            score = 0.3; // Assign a score of 0.3 to p elements
        } else {
            score = 1.0; // Assign a score of 1.0 to div elements
        }
        // Add more conditions to assign scores based on other element types

        return score;
    }


    private static void printInvertedIndex(Map<String, Map<String, word_par>> invertedIndex) {
        for (Map.Entry<String, Map<String, word_par>> entry : invertedIndex.entrySet()) {
            String word = entry.getKey();
            Map<String, word_par> documentScores = entry.getValue();
            System.out.println("Word: " + word);
            for (Map.Entry<String, word_par> docEntry : documentScores.entrySet()) {
                String docName = docEntry.getKey();
                double score = docEntry.getValue().score;
                System.out.println("\tDocName: " + docName + ", Score: " + score);
            }
        }
    }

    public static void uploadToDB(Map<String, Map<String, Double>> invertedIndex) {
        mongoDBClient.uploadIndexer(invertedIndex);
        System.out.println("Upload to MongoDB completed.");
    }

    private String removeStopWords(String query) {
        String[] words = query.split(" ");
        String result = "";
        for (String word : words) {
            boolean isStopWord = false;
            for (String stopWord : stopWords) {
                if (word.equals(stopWord)) {
                    isStopWord = true;
                    break;
                }
            }
            if (!isStopWord) {
                result += word + " ";
            }
        }
        return result;
    }


    private String removePunctuation(String query) {
        String[] words = query.split(" ");
        String result = "";
        for (String word : words) {
            boolean isPunctuation = false;
            for (String punctuation : punctuations) {
                if (word.equals(punctuation)) {
                    isPunctuation = true;
                    break;
                }
                if (word.contains(punctuation)) {
                    word = word.replace(punctuation, "");
                }
            }
            if (!isPunctuation) {
                result += word + " ";
            }
        }
        return result;
    }

    private String stem(String query) {
        PorterStemmer stemmer = new PorterStemmer();
        String[] words = query.split(" ");
        String result = "";
        for (String word : words) {
            stemmer.add(word.toCharArray(), word.length());
            stemmer.stem();
            result += stemmer.toString() + " ";
        }
        return result;
    }
    private static void calculateTF_IDF(Map<String, Map<String, word_par>> invertedIndex){
        //Tf*IDF
        //tf: term frequency
        //IDF: /DF
        for (String word : invertedIndex.keySet()) {
            // Calculate the IDF
            //IDF=doc numbers/DF
            double IDF = (double) document_numbers / (double) invertedIndex.get(word).size();
            for (String doc : invertedIndex.get(word).keySet()) {
                // calculate the normalized tf
               double tf = invertedIndex.get(word).get(doc).TF;
                double normalizedTF = tf / (double) invertedIndex.get(word).get(doc).size;
                // Store the TF_IDF
                invertedIndex.get(word).get(doc).TF_IDF = normalizedTF * IDF;
            }
        }
    }

    }


    private class PhraseSearcher {
        String[] phrases;

        PhraseSearcher() {
            System.out.println("PhraseSearcher initialized");
        }

        public Boolean HasPhrases(String query) {
            return query.contains("\"");
        }

        public String[] GetPhrases(String query) {
            phrases = query.split("\"");
            for (String phrase : phrases) {
                System.out.println(phrase);
            }
            return phrases;
        }

        public String RemovePhrases(String query) {
            for (String phrase : phrases) {
                query = query.replace("\"" + phrase + "\"", "");
            }
            return query;
        }

    }


    public void ProcessPharse (String phrase) {
        // TODO: process phrase
    }

}


