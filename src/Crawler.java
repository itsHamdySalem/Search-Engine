
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.Set;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class Crawler implements Runnable {
    
    private int numVisitedPages;
    private static final int MAX_WEB_PAGES = 10;
    
    private static Set<String> pagesVisited = new ConcurrentSkipListSet<>();
    private static Queue<String> pagesToVisit = new ConcurrentLinkedQueue<>();

    private static Object getContentLock = new Object();
    private static Object pagesVisitedLock = new Object();
    private static Object pagesToVisitLock = new Object();
    
    MongoDB mongoDBClient;

    public Crawler() {
        mongoDBClient = new MongoDB();
        mongoDBClient.connectToDatabase();
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " has started..");
        while (true) {
            synchronized (pagesVisitedLock) {
                if (pagesVisited.size() >= MAX_WEB_PAGES) {
                    break;
                }
            }

            String url;
            synchronized (pagesToVisitLock) {
                url = pagesToVisit.poll();
                mongoDBClient.updatePagesToVisit(url);
            }

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
                    synchronized (pagesVisitedLock) {
                        if (pagesVisited.size() >= MAX_WEB_PAGES) {
                            break;
                        }

                        pagesVisited.add(normalizedUrl);
                        mongoDBClient.updatePagesVisited(normalizedUrl);
                        mongoDBClient.updateNumPagesVisited(pagesVisited.size());

                        synchronized (getContentLock) {
                            getPageContent(doc, normalizedUrl);
                        }
                        
                        System.out.println(Thread.currentThread().getName() + ":");
                        System.out.println("Link: " + normalizedUrl);
                        System.out.println("Title: " + doc.title());
                        
                        if (pagesVisited.size() >= MAX_WEB_PAGES) {
                            break;
                        }
                    }

                    for (Element link : doc.select("a[href]")) {
                        String nextUrl = link.attr("abs:href");

                        try {
                            // Normalize the next URL
                            URI nextUri = new URI(nextUrl);
                            String normalizedNextUrl = nextUri.normalize().toString();

                            // Add the next URL to the queue
                            synchronized (pagesVisitedLock) {
                                if (pagesVisited.contains(normalizedNextUrl)) {
                                    continue;
                                }

                                synchronized (pagesToVisitLock) {
                                    pagesToVisit.add(normalizedNextUrl);
                                    mongoDBClient.updatePagesToVisit(normalizedNextUrl);
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
        System.out.println(Thread.currentThread().getName() + " has finished..");
    }

    public void crawl() {
        if (mongoDBClient.getState().equals("crawling")) {
            numVisitedPages = mongoDBClient.getPagesVisited(pagesToVisit, pagesVisited);
        }

        if (pagesToVisit.isEmpty()) {
            getPagesToVisit();
        }

        mongoDBClient.setState("crawling");

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter number of threads: ");
            int numThreads = scanner.nextInt();
            Thread[] crawlingThread = new Thread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                crawlingThread[i] = new Thread(this);
                crawlingThread[i].setName("Thread " + i);
                crawlingThread[i].start();
            }

            for (int i = 0; i < numThreads; i++) {
                try {
                    crawlingThread[i].join();
                } catch (InterruptedException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        mongoDBClient.setState("idle");

        System.out.println("overall pages visited = " + pagesVisited.size());
        System.out.println("crawling finished..");
    }

    private static void getPagesToVisit() {
        try {
            File file = new File("src/seed.txt");
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

    private void getPageContent(Document doc, String url) {
        final String path = "E:/CUFE/2- Junior CMP/Second Term/CMP 2050/Project/sandbox/downloadedPages/";
        
        String fileName = url.substring(0, url.length());
        fileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        try {
            File file = new File(path + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            writer.write(url + "\n");
            writer.write(doc.html());
            writer.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
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