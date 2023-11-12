package org.ungs;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import entities.Shop;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GarbarinoScraper extends Shop {

    String shopUrl = "src/resources/garbarino-mouse.html";
    //String shopUrl = "https://www.garbarino.com";
    public GarbarinoScraper() {}

    @Override
    public Set<Map<String, BigDecimal>> search(String productName) {
        if (productName.isEmpty()) {
            return new HashSet<>();
        }

        String currentUrlSearch = shopUrl;

        if (isValidWebUrl(shopUrl)) {
            currentUrlSearch = shopUrl + "/shop/sort-by-price-low-to-high?search=" + productName.replace(" ", "%20");
        }

        Set<Callable<Set<Map<String, BigDecimal>>>> tasks = new HashSet<>();

        try {
            Set<Map<String, BigDecimal>> articles = scrapeProductsFromPage(currentUrlSearch, productName);
            tasks.add(() -> articles);
        } catch (Exception ignored) {}

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Set<Future<Set<Map<String, BigDecimal>>>> futures;

        try {
            futures = new HashSet<>(executorService.invokeAll(tasks));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new HashSet<>();
        } finally {
            executorService.shutdown();
        }

        Set<Map<String, BigDecimal>> result = new HashSet<>();
        for (Future<Set<Map<String, BigDecimal>>> future : futures) {
            try {
                result.addAll(future.get());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }

        return result;
    }

    private Set<Map<String, BigDecimal>> scrapeProductsFromPage(String urlSearch, String productName) {
        Set<Map<String, BigDecimal>> elements = new HashSet<>();

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
                    double price = Double.parseDouble(priceStr);
                    BigDecimal priceBd = new BigDecimal(Double.toString(price));

                    if (normalizeString(name).contains(normalizeString(productName))) {
                        Map<String, BigDecimal> articleMap = new HashMap<>();
                        articleMap.put(name, priceBd);

                        elements.add(articleMap);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return elements;
    }

    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }

    private Document getDocumentHtml(String shopUrl) throws IOException {
        if (isValidWebUrl(shopUrl)){
            Connection connection = Jsoup.connect(shopUrl);
            connection.header("Content-Type", "text/html; charset=UTF-8");
            return connection.get();
        }

        File input = new File(shopUrl);
        return Jsoup.parse(input, "UTF-8", "");
    }

    private boolean isValidWebUrl(String url){
        return url.contains("www.") || url.contains("http");
    }
}