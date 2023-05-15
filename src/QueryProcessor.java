
public class QueryProcessor {

    PhraseSearcher phraseSearcher = new PhraseSearcher();

    String[] stopWords = {"a", "an", "the", "is", "are", "am", "was", "were", "has", "have", "had", "been", "will", "shall", "be", "do", "does", "did", "can", "could", "may", "might", "must", "should", "of", "in", "on", "at", "to", "from", "by", "for", "about", "with", "without", "not", "no", "yes", "or", "and", "but", "if", "else", "then", "than", "else", "when", "where", "what", "who", "how", "which", "whom", "whose", "why", "because", "however", "therefore", "thus", "so", "such", "this", "that", "these", "those", "their", "his", "her", "its", "our", "your", "their", "any", "some", "many", "much", "few", "little", "own", "other", "another", "each", "every", "all", "both", "neither", "either", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth"};
    String[] punctuations = {".", ",", ":", ";", "?", "!", "'", "\"", "(", ")", "{", "}", "[", "]", "<", ">", "/", "\\", "|", "-", "_", "+", "=", "*", "&", "^", "%", "$", "#", "@", "`", "~", "“", "”", "‘", "’", "–", "—", "…"};
    
    public static void main (String[] args) {
        QueryProcessor queryProcessor = new QueryProcessor();
        // TODO: add test cases
    }

    QueryProcessor() {
        System.out.println("QueryProcessor initialized");
    }

    public void process(String query) {
        PreProcess(query);
        // TODO: process pharses and remove them from query
        // TODO: process query
        System.out.println(query);
    }

    private String PreProcess (String query) {
        if (phraseSearcher.HasPhrases(query)) {
            phraseSearcher.GetPhrases(query);
            query = phraseSearcher.RemovePhrases(query);

        }
        query = query.toLowerCase();
        query = removeStopWords(query);
        query = removePunctuation(query);
        query = stem(query);
    }

    private String removeStopWords (String query) {
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

    private String removePunctuation (String query) {
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

    private String stem (String query) {
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

    private class PhraseSearcher {
        String[] phrases;

        PhraseSearcher() {
            System.out.println("PhraseSearcher initialized");
        }

        public Boolean HasPhrases (String query) {
            return query.contains("\"");
        }

        public String[] GetPhrases (String query) {
            phrases = query.split("\"");
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

        public void ProcessPharse (String phrase) {
            // TODO: process phrase
        }
    
    }

}
