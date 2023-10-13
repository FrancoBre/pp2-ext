package org.ungs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import shoppinator.core.interfaces.Scraper;

public class GarbarinoScraper extends Scraper {

    public GarbarinoScraper() {
    }

    @Override
    public String scrap(String productName) {

        if (productName.isEmpty()) {
            return "[]";
        }

        String currentUrlSearch = this.getUrl() +"/shop/sort-by-price-low-to-high?search="+ productName.replace(" ", "%20");
        List<Callable<List<GenericElementGarbarino>>> tasks = new ArrayList<>();

        try {
            List<GenericElementGarbarino> genericElements = scrapeProductsFromPage(currentUrlSearch, productName);
            tasks.add(() -> genericElements);
        } catch (Exception ignored) {}

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<GenericElementGarbarino>>> futures;

        try {
            futures = executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            executorService.shutdown();
        }

        List<GenericElementGarbarino> genericElementsList = new ArrayList<>();
        for (Future<List<GenericElementGarbarino>> future : futures) {
            try {
                genericElementsList.addAll(future.get());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        return generateJson(genericElementsList);
    }

    private List<GenericElementGarbarino> scrapeProductsFromPage(String urlSearch, String productName) {
        Map<String, GenericElementGarbarino> genericElementsMap = new LinkedHashMap<>();//LinkedHashMap para mantener el orden y que no haya repes

        try {
            Connection connection = Jsoup.connect(urlSearch);
            connection.header("Content-Type", "text/html; charset=UTF-8");
            Document documentHtml = connection.get();
            Elements articleElements = documentHtml.select(".product-card-design6-vertical");

            for (Element articleElement : articleElements) {
                String name = articleElement.select("div.product-card-design6-vertical__name").text();
                String priceStr = articleElement.select("div.product-card-design6-vertical__price span").text()
                    .replace("$", "").replace(".", "").replace(",", ".");

                if (!name.isEmpty() && !priceStr.isEmpty()) {

                    double price = Double.parseDouble(priceStr);
                    Element linkImg = articleElement.select("a").first();
                    String postUrl = (linkImg != null) ? (this.getUrl() + linkImg.attr("href")) : "";
                    String imageUrl = articleElement.select("img[src]").attr("src");

                    if (normalizeString(name).contains(normalizeString(productName)) && !imageUrl.isEmpty()) {
                        GenericElementGarbarino genericElement = new GenericElementGarbarino(name, postUrl, price, imageUrl);
                        genericElementsMap.put(name, genericElement);
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>(genericElementsMap.values());
    }

    private String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }

    public String generateJson(List<GenericElementGarbarino> genericElements) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(genericElements);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }
}

@Data
@AllArgsConstructor
class GenericElementGarbarino {

    private String name;
    private String postUrl;
    private double price;
    private String productImageUrl;
}