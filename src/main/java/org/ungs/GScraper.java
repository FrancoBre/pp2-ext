package org.ungs;

import shoppinator.core.interfaces.Scraper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.IOException;
import java.text.Normalizer;
import java.util.regex.Pattern;
public class GScraper extends Scraper {
    public GScraper() {}
    @Override
    public String scrap(String productName) {
        //TODO obtener urls por properties

        if (productName.isEmpty()) {
            return "[]";
        }

        String currentUrlSearch = "https://www.garbarino.com/shop/sort-by-price-low-to-high?search=" + productName.replace(" ", "%20");

        List<Callable<List<GenericElement>>> tasks = new ArrayList<>();

        try{
            List<GenericElement> genericElements = scrapeProductsFromPage(currentUrlSearch, productName);
            tasks.add(() -> genericElements);
        }
        catch (Exception ignored){}

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<GenericElement>>> futures;

        try {
            futures = executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            executorService.shutdown();
        }

        List<GenericElement> genericElementsList = new ArrayList<>();
        for (Future<List<GenericElement>> future : futures) {
            try {
                genericElementsList.addAll(future.get());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        return generateJson(genericElementsList);
    }

    private List<GenericElement> scrapeProductsFromPage(String urlSearch, String productName) {
        Map<String, GenericElement> genericElementsMap = new LinkedHashMap<>();//LinkedHashMap para mantener el orden y que no haya repes

        try {
            Connection connection = Jsoup.connect(urlSearch);
            connection.header("Content-Type", "text/html; charset=UTF-8");
            Document documentHtml = connection.get();
            Elements articleElements = documentHtml.select("div[data-v-09ba2a60]");

            for (Element articleElement : articleElements) {
                String name = articleElement.select("div.product-card-design2-vertical__name").text();
                String priceStr = articleElement.select("div.product-card-design2-vertical__price span").text()
                        .replace("$", "")
                        .replace(".", "")
                        .replace(",", ".");

                if (!name.isEmpty() && !priceStr.isEmpty()){

                    double price = Double.parseDouble(priceStr);
                    Element linkImg = articleElement.select("a").first();
                    String postUrl = (linkImg != null) ? ("https://www.garbarino.com" + linkImg.attr("href")) : "";
                    String imageUrl = articleElement.select("img[src]").attr("src");

                    if (normalizeString(name).contains(normalizeString(productName)) && !imageUrl.isEmpty()) {
                        GenericElement genericElement = new GenericElement(name, postUrl, price, imageUrl);
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

    public String generateJson(List<GenericElement> genericElements) {
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
class GenericElement {
    private String name;
    private String postUrl;
    private double price;
    private String productImageUrl;
}