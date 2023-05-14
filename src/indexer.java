



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

//To Do : handle the public global variables


 class DocumentCollector {
    public static void main(String[] args) {
        //To Do: we need to put the path to the documents
        String directoryPath = "/path/to/documents";

        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        collectDocument(file);
                    }
                }
            }
        }
    }

    private static void collectDocument(File file) {
        //To Do :handle the syncronization
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder document = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {

                document.append(line).append("\n");
                /*
                 * absabsd
                 * aksdhalksjdn
                 * asdlknhasd
                 * */
            }
            // Process the collected document as needed
            System.out.println("Collected document: " + document.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class indexer implements Runnable{


    @Override
    public void run() {

    }
}
