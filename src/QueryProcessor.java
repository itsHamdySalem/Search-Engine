
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;

public class QueryProcessor {

    String query;
    String[] phrases;
    
    Set<String> stopWordsSet;
    Set<Character> punctuationsSet;
    
    PhraseSearcher phraseSearcher = new PhraseSearcher();
    
    static final String[] stopWords = {"a", "an", "the", "is", "are", "am", "was", "were", "has", "have", "had", "been", "will", "shall", "be", "do", "does", "did", "can", "could", "may", "might", "must", "should", "of", "in", "on", "at", "to", "from", "by", "for", "about", "with", "without", "not", "no", "yes", "or", "and", "but", "if", "else", "then", "than", "else", "when", "where", "what", "who", "how", "which", "whom", "whose", "why", "because", "however", "therefore", "thus", "so", "such", "this", "that", "these", "those", "their", "his", "her", "its", "our", "your", "their", "any", "some", "many", "much", "few", "little", "own", "other", "another", "each", "every", "all", "both", "neither", "either", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth"};
    static final Character[] punctuations = {'.', ',', ':', ';', '?', '!', '\'', '\"', '(', ')', '{', '}', '[', ']', '<', '>', '/', '\\', '|', '-', '_', '+', '=', '*', '&', '^', '%', '$', '#', '@', '`', '~', '“', '”', '‘', '’', '–', '—', '…'};

    public static void main (String[] args) {
        QueryProcessor queryProcessor = new QueryProcessor();
        // Test cases
        queryProcessor.ProcessQuery("This is a test query.");
        System.out.println();
        queryProcessor.ProcessQuery("\"Search for this phrase.\"");
        System.out.println();
        queryProcessor.ProcessQuery("\"Java programming\" AND \"web development\"");
        System.out.println();
        queryProcessor.ProcessQuery("I want to search for a book on Java programming!");
        System.out.println();
        queryProcessor.ProcessQuery("\"Search for this phrase.\" and \"Search for this phrase.\"");
        System.out.println();
        queryProcessor.ProcessQuery("\"OpenAI's language model, GPT-3, is a game-changer!\"");
    }

    QueryProcessor() {
        stopWordsSet = new HashSet<>(Arrays.asList(stopWords));
        punctuationsSet = new HashSet<>(Arrays.asList(punctuations));
        System.out.println("QueryProcessor initialized...");
    }

    public void ProcessQuery(String query) {
        query = PreProcessQuery(query);
        System.out.println("Query after preprocessing: " + query);
        if (phrases != null) {
            System.out.println("Phrases:");
            for (String phrase : phrases) {
                System.out.println(phrase);
            }
        }
        // TODO: process query and phrases
    }

    private String PreProcessQuery (String query) {
        if (phraseSearcher.HasPhrases(query)) {
            phrases = phraseSearcher.GetPhrases(query);
            query = phraseSearcher.RemovePhrases(query);
        }
        query = query.toLowerCase();
        query = removeStopWords(query);
        query = removePunctuation(query);
        // query = stem(query);
        return query;
    }

    private String removeStopWords (String query) {
        String[] words = query.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (stopWordsSet.contains(word)) {
                continue;
            }
            result.append(word).append(" ");
        }
        return result.toString().trim();
    }

    private String removePunctuation (String query) {
        String[] words = query.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() == 0) {
                continue;
            }
            if (word.length() == 1 && punctuationsSet.contains(word.charAt(0))) {
                continue;
            }
            if (punctuationsSet.contains(word.charAt(word.length() - 1))) {
                word = word.substring(0, word.length() - 1);
            }
            if (punctuationsSet.contains(word.charAt(0))) {
                word = word.substring(1);
            }
            result.append(word).append(" ");
        }
        return result.toString().trim();
    }

    // private String stem (String query) {
    //     PorterStemmer stemmer = new PorterStemmer();
    //     String[] words = query.split(" ");
    //     StringBuilder result = new StringBuilder();
    //     for (String word : words) {
    //         stemmer.setCurrent(word);
    //         stemmer.stem();
    //         result.append(stemmer.getCurrent()).append(" ");
    //     }
    //     return result.toString().trim();
    // }

    private class PhraseSearcher {

        PhraseSearcher() {
            System.out.println("PhraseSearcher initialized...");
        }

        public Boolean HasPhrases (String query) {
            return query.contains("\"");
        }

        public String[] GetPhrases (String query) {
            String[] phrases = query.split("\"");
            System.out.println("Phrases:");
            for (String phrase : phrases) {
                System.out.println(phrase);
            }
            return phrases;
        }

        public String RemovePhrases (String query) {
            for (String phrase : phrases) {
                query = query.replace("\"" + phrase + "\"", "");
            }
            return query;
        }

        public void ProcessPhrase (String phrase) {
            // TODO: process phrase
        }
    
    }

}
