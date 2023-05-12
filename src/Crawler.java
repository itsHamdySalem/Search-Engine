import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;

import java.util.Set;
import java.util.Queue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.LinkedList;

import java.net.URI;

import javax.print.Doc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.io.FileNotFoundException;

public class Crawler implements Runnable {

    private static final int MAX_WEB_PAGES = 6000;
    private static int crawledPages = 0;
    
    private static Set<String> pagesVisited = new HashSet<String>();
    private static Queue<String> pagesToVisit = new LinkedList<String>();
    
    // TODO: store in pagesPopularity the popularity of web pages 
    // private HashMap<String, Integer> pagesPopularity = new HashMap<String, Integer>();

    // TODO: implement MonogDB class and RobotCheck class to use here
    // private MongoDB database = new MongoDB();
    // private RobotCheck robot_object = new RobotCheck();

    public static void main(String[] args) throws IOException {
        crawl();
    }

    public Crawler() {
        // TODO: initialize Crawler variables
        // database.connect();
    }

    @Override
    public void run() {
        // TODO: implement this function
    }

    private static void crawl() {
        // TODO: make it multithreaded
        if (crawledPages >= MAX_WEB_PAGES) return;
        if (crawledPages == 0) getPagesToVisit();
        while (!pagesToVisit.isEmpty()) {
            String url = pagesToVisit.poll();
            try {
                // Validate the URL
                if (!isValid(url)) {
                    System.out.println("Invalid URL: " + url);
                    continue;
                }
                // Skip URLs starting with 'javascript:'
                if (url.startsWith("javascript:")) {
                    System.out.println("Skipping JavaScript URL: " + url);
                    continue;
                }
                // Normalize the URL
                URI uri = new URI(url);
                String normalizedUrl = uri.normalize().toString();
                // Connect to the URL
                Connection con = Jsoup.connect(normalizedUrl);
                Document doc = con.get();
                if (con.response().statusCode() == 200) {   // 200 is the HTTP OK status code
                    System.out.println("Link: " + normalizedUrl);
                    System.out.println(doc.title());
                    pagesVisited.add(normalizedUrl);
                    crawledPages++;
                    // TODO: implement database and robot check
                    for (Element link : doc.select("a[href]")) {
                        String nextUrl = link.attr("abs:href");
                        // TODO: implement robot check
                        try {
                            // Normalize the next URL
                            URI nextUri = new URI(nextUrl);
                            String normalizedNextUrl = nextUri.normalize().toString();
                            // Add the next URL to the queue
                            if (!pagesVisited.contains(normalizedNextUrl)) {
                                pagesToVisit.add(normalizedNextUrl);
                            }
                        } catch (URISyntaxException e) {
                            System.out.println("Invalid URL: " + nextUrl);
                        }
                    }
                }
            } catch (IOException | URISyntaxException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static boolean isValid(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void get_html_content (Document doc, String url) {
        final String path = "downloadedPages/";
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        try {
            File file = new File(path + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.write(doc.html());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void getPagesToVisit() {
        pagesToVisit = new LinkedList<String>();
        try {
            File file = new File("seed.txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String url = scanner.nextLine();
                pagesToVisit.add(url);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}



