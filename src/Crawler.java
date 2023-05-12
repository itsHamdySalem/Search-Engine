import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Queue;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.LinkedList;

import java.net.URI;
import java.net.URISyntaxException;

import javax.print.Doc;

public class Crawler implements Runnable {

    private static final int MAX_WEB_PAGES = 24;
    private static int crawledPages = 0;
    
    private static Set<String> pagesVisited = new ConcurrentSkipListSet<>();
    private static Queue<String> pagesToVisit = new ConcurrentLinkedQueue<>();
    
    // TODO: store in pagesPopularity the popularity of web pages 
    // private HashMap<String, int> pagesPopularity = new HashMap<String, int>();

    // TODO: implement MonogDB class and RobotCheck class to use here
    // private MongoDB database = new MongoDB();
    // private RobotCheck robot_object = new RobotCheck();

    public static void main(String[] args) throws IOException {
        crawl();
        System.out.println("pagesvisited = " + pagesVisited.size());
        for (String url : pagesVisited) {
            System.out.println(url);
        }
    }

    public Crawler () {
        // TODO: initialize Crawler variables
        // database.connect();
    }

    @Override
    public void run () {
        // TODO: implement run method
    }

    private static void crawl() {
        if (crawledPages >= MAX_WEB_PAGES) return;
        if (crawledPages == 0 && pagesToVisit.isEmpty()) getPagesToVisit();
        
        int currentThread = 0;
        int numThreads = pagesToVisit.size();
        int numPagesPerThread = MAX_WEB_PAGES / numThreads;
        Thread[] crawlingThread = new Thread [numThreads];
        
        while (!pagesToVisit.isEmpty()) {
            crawlingThread[currentThread] = new Thread (new threadedCrawler(pagesToVisit.poll(), numPagesPerThread));
            crawlingThread[currentThread].setName("Thread " + currentThread);
            currentThread++;
        }
        
        for (int i = 0; i < numThreads; i++) {
            crawlingThread[i].start();
        }

        for (int i = 0; i < numThreads; i++) {
            try {
                crawlingThread[i].join();
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
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
        pagesToVisit = new ConcurrentLinkedQueue<>();
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

    private static class threadedCrawler implements Runnable {
        
        private Queue<String> pagesToVisit = new ConcurrentLinkedQueue<>();
        private int crawledPages;
        private int maxPages;
        private static Object lock = new Object();

        public threadedCrawler(String seed, int maxPages) {
            this.pagesToVisit.add(seed);
            this.maxPages = maxPages;
            this.crawledPages = 0;
        }

        @Override
        public void run() {
            crawl();
        }

        private void crawl() {
            while (!this.pagesToVisit.isEmpty() && this.crawledPages < maxPages) {
                String url;
                // synchronized (lock) {
                    url = this.pagesToVisit.poll();
                // }
                try {
                    // Validate the URL
                    if (!isValid(url)) {
                        System.out.println(Thread.currentThread().getName() + ":");
                        System.out.println("Invalid URL: " + url);
                        continue;
                    }
                    // Skip URLs starting with 'javascript:'
                    if (url.startsWith("javascript:")) {
                        System.out.println(Thread.currentThread().getName() + ":");
                        System.out.println("Skipping JavaScript URL: " + url);
                        continue;
                    }
                    // Normalize the URL
                    URI uri = new URI(url);
                    String normalizedUrl = uri.normalize().toString();
                    // Connect to the URL
                    Connection con = Jsoup.connect(normalizedUrl);
                    Document doc = con.get();
                    if (con.response().statusCode() == 200) { // 200 is the HTTP OK status code
                        System.out.println(Thread.currentThread().getName() + ":");
                        System.out.println("Link: " + normalizedUrl);
                        System.out.println(doc.title());
                        synchronized (lock) {
                            pagesVisited.add(normalizedUrl);
                        }
                        this.crawledPages++;
                        if (this.crawledPages >= maxPages) {
                            return;
                        }
                        for (Element link : doc.select("a[href]")) {
                            String nextUrl = link.attr("abs:href");
                            try {
                                // Normalize the next URL
                                URI nextUri = new URI(nextUrl);
                                String normalizedNextUrl = nextUri.normalize().toString();
                                // Add the next URL to the queue
                                synchronized (lock) {
                                    if (!pagesVisited.contains(normalizedNextUrl)) {
                                        this.pagesToVisit.add(normalizedNextUrl);
                                    }
                                }
                            } catch (URISyntaxException e) {
                                System.out.println(Thread.currentThread().getName() + ":");
                                System.out.println("Invalid URL: " + nextUrl);
                            }
                        }
                    }
                } catch (IOException | URISyntaxException e) {
                    System.out.println(Thread.currentThread().getName() + ":");
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
    }

}

