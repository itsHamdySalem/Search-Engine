import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;

import javax.sound.sampled.EnumControl;

public class Ranker {
    // TODO: this should be replaced by the actual number of documents we have.
    final static int numberOfDocs = 3;
    static int[] ids = new int[numberOfDocs];
    public static void Rank(String query) {
        JSONParser parser = new JSONParser();
        try {
            // put the path to the json file here
            Object obj = parser.parse(new FileReader("D:\\Java Projects\\RANKER\\DB.json"));
            JSONObject jsonObject = (JSONObject)obj;
            // TODO: this should be replaced by the words in the query.
            String[] words = {"hamdy", "lol","sory", "lol2"};
            int[] scores = new int[numberOfDocs];
            for (String word : words) {
                if (!jsonObject.containsKey(word)) continue;
                JSONArray docs = (JSONArray)jsonObject.get(word);
                docs.forEach( doc -> {
                    JSONObject currentDoc = (JSONObject)doc;
                    long id = (long) currentDoc.get("ID");
                    long TF_IDF = (long)currentDoc.get("TF_IDF");
                    long freq = (long)currentDoc.get("freq");
                    scores[(int)id]+=TF_IDF*freq;
                });
            }

            Integer[] indices = new Integer[ids.length];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = i;
            }

            Arrays.sort(indices, Comparator.comparingInt(index -> scores[(int)index]).reversed());

            ids = Arrays.stream(indices).mapToInt(index -> ids[index]).toArray();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        // ids array is the array that will store the order of the docs ids in the wanted way
        // initially it's filled with the order starting from zero
        // note that I'm assuming the ids are zero-indexed.
        for (int i = 0; i < numberOfDocs; i++) {
            ids[i] = i;
        }
        Rank("dum");
//        System.out.println(Arrays.toString(ids));
    }
}
