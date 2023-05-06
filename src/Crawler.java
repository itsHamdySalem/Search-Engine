import org.jsoup.Connection;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;

public class Crawler {

    public static void main(String[] args) throws IOException {
        String url = "https://en.wikipedia.org/";
        crawl(1, url, new ArrayList<String>());
    }

    private static void crawl(int level, String url, ArrayList<String> visited) {
        if (level <= 5) {
            Document doc = request(url, visited);

            if (doc != null) {
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String next_url = link.absUrl("href");
                    if (!visited.contains(next_url)) {
                        crawl(level++, next_url, visited);
                    }
                }
            }
        }
    }

    private static Document request(String url, ArrayList<String> visited) {
        try {
            Connection con = Jsoup.connect(url);
            Document doc = con.get();

            if (con.response().statusCode() == 200) {
                System.out.println("Link: " + url);
                System.out.println(doc.title());
                visited.add(url);
                return doc;
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
