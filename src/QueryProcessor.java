
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import org.tartarus.snowball.ext.PorterStemmer;


public class QueryProcessor {

    static final String[] stopWords = {"a", "an", "the", "is", "are", "am", "was", "were", "has", "have", "had", "been", "will", "shall", "be", "do", "does", "did", "can", "could", "may", "might", "must", "should", "of", "in", "on", "at", "to", "from", "by", "for", "about", "with", "without", "not", "no", "yes", "or", "and", "but", "if", "else", "then", "than", "else", "when", "where", "what", "who", "how", "which", "whom", "whose", "why", "because", "however", "therefore", "thus", "so", "such", "this", "that", "these", "those", "their", "his", "her", "its", "our", "your", "their", "any", "some", "many", "much", "few", "little", "own", "other", "another", "each", "every", "all", "both", "neither", "either", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth"};
    static final Character[] punctuations = {'.', ',', ':', ';', '?', '!', '\'', '\"', '(', ')', '{', '}', '[', ']', '<', '>', '/', '\\', '|', '-', '_', '+', '=', '*', '&', '^', '%', '$', '#', '@', '`', '~', '“', '”', '‘', '’', '–', '—', '…'};

    String query;
    String[] phrases;
    
    static final Set<String> stopWordsSet = new HashSet<>(Arrays.asList(stopWords));
    static final Set<Character> punctuationsSet = new HashSet<>(Arrays.asList(punctuations));
    
    PhraseSearcher phraseSearcher = new PhraseSearcher();

    QueryProcessor(){
        
    }


    public String ProcessQuery(String query) {
        return PreProcessQuery(query);
/*        System.out.println("Query after preprocessing: " + query);
        if (phrases != null) {
            System.out.println("Phrases:");
            for (String phrase : phrases) {
                System.out.println(phrase);
            }
        }
        // TODO: process query and phrases
        // just call phraseSearcher.ProcessPhrase(phrase) for each phrase and process query normally
*/
    }

    private String PreProcessQuery (String query) {
        if (phraseSearcher.HasPhrases(query)) {
            phrases = phraseSearcher.GetPhrases(query);
            query = phraseSearcher.RemovePhrases(query);
        }
        query = query.toLowerCase();
        query = removeStopWords(query);
        query = removePunctuation(query);
        query = stem(query);
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
            StringBuilder wordBuilder = new StringBuilder();
            for (int i = 0; i < word.length(); i++) {
                if (!punctuationsSet.contains(word.charAt(i))) {
                    wordBuilder.append(word.charAt(i));
                }
            }
            word = wordBuilder.toString();
            result.append(word).append(" ");
        }
        return result.toString().trim();
    }
    public static String stem(String word){
        PorterStemmer porterStemmer = new PorterStemmer();
        porterStemmer.setCurrent(word);
        porterStemmer.stem();
        return porterStemmer.getCurrent();
    }

    private class PhraseSearcher {

        PhraseSearcher() {
            // Empty constructor
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


