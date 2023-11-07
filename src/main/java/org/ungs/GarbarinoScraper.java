package org.ungs;

import entities.Product;
import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import entities.ProductPresentation;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import entities.Shop;

public class GarbarinoScraper extends Shop {

    //String shopUrl = "src/resources/garbarino-mouse.html";
    String shopUrl = "https://www.garbarino.com";
    public GarbarinoScraper() {}

    @Override
    public Set<Product> search(String productName) {

        if (productName.isEmpty()) {
            return new HashSet<>();
        }

        String currentUrlSearch = shopUrl;

        if (shopUrl.contains("www.")){
            currentUrlSearch = shopUrl + "/shop/sort-by-price-low-to-high?search=" + productName.replace(" ", "%20");
        }

        Set<Callable<Set<Product>>> tasks = new HashSet<>();

        try {
            Set<Product> products = scrapeProductsFromPage(currentUrlSearch, productName);
            tasks.add(() -> products);
        } catch (Exception ignored) {}

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Set<Future<Set<Product>>> futures;

        try {
            futures = new HashSet<>(executorService.invokeAll(tasks));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new HashSet<>();
        } finally {
            executorService.shutdown();
        }

        Set<Product> products = new HashSet<>();
        for (Future<Set<Product>> future : futures) {
            try {
                products.addAll(future.get());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        return products;
    }

    private Set<Product> scrapeProductsFromPage(String urlSearch, String productName) {
        Set<Product> products = new HashSet<>();

        try {
            Document documentHtml = getDocumentHtml(urlSearch);
            Elements articleElements = documentHtml.select(".product-card-design6-vertical");

            for (Element articleElement : articleElements) {
                String name = articleElement.select("div.product-card-design6-vertical__name").text();
                String priceStr = articleElement.select("div.product-card-design6-vertical__price span").text()
                        .replace("$", "")
                        .replace(".", "")
                        .replace(",", ".")
                        .replace(" ", "");

                if (!name.isEmpty() && !priceStr.isEmpty()) {
                    Long price = Long.parseLong(priceStr);
                    Element linkImg = articleElement.select("a").first();
                    String postUrl = (linkImg != null) ? ("https://www.garbarino.com" + linkImg.attr("href")) : "";
                    String imageUrl = articleElement.select("img[src]").attr("src");

                    if (normalizeString(name).contains(normalizeString(productName)) && !imageUrl.isEmpty()) {
                        Product product = new Product(name, this.getName(), new ProductPresentation(price, postUrl, imageUrl));
                        products.add(product);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return products;
    }

    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }

    private Document getDocumentHtml(String shopUrl) throws IOException {
        if (shopUrl.contains("www.")){
            Connection connection = Jsoup.connect(shopUrl);
            connection.header("Content-Type", "text/html; charset=UTF-8");
            return connection.get();
        }

        File input = new File(shopUrl);
        return Jsoup.parse(input, "UTF-8", "");
    }
}