package org.ungs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Scraper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

public class FravegaScraper extends Scraper {

    public FravegaScraper() {
    }

    @Override
    public String scrap(String productName) {

        if (productName.isEmpty()) {
            return "[]";
        }

        String currentUrlSearch = this.getUrl()+"/l/?keyword=" + productName.replace(" ","+") +
                "&sorting=LOWEST_SALE_PRICE&page=";
        List<Callable<List<GenericElementFravega>>> tasks = new ArrayList<>();

        int pageNum = 1;
        boolean productsExists = true;

        while(productsExists) {//Recorremos cada una de las paginas de la tienda
            String urlIt = (currentUrlSearch + pageNum);
            List<GenericElementFravega> elements = scrapeProductsFromPage(urlIt, productName);
            pageNum++;
            productsExists = !elements.isEmpty();//Si llego al final de las paginas, salimos
            tasks.add(() -> elements);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<List<GenericElementFravega>>> futures;

        try {
            futures = executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            executorService.shutdown();
        }

        List<GenericElementFravega> genericElementsList = new ArrayList<>();
        for (Future<List<GenericElementFravega>> future : futures) {
            try {
                genericElementsList.addAll(future.get());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        return generateJson(genericElementsList);
    }

    private List<GenericElementFravega> scrapeProductsFromPage(String urlSearch, String productName) {
        Map<String, GenericElementFravega> genericElementsMap = new LinkedHashMap<>();//LinkedHashMap para mantener el orden y que no haya repes

        try {
            Connection connection = Jsoup.connect(urlSearch);
            connection.header("Content-Type", "text/html; charset=UTF-8");
            Document documentHtml = connection.get();
            Elements articleElements = documentHtml.select("article.sc-ef269aa1-2.FmCUT");

            for (Element articleElement : articleElements) {
                String name = articleElement.select("span.sc-6321a7c8-0.jKvHol").text();
                String priceStr = articleElement.select("div.sc-854e1b3a-0.kfAWhD span.sc-ad64037f-0.ixxpWu").text()
                        .replace("$", "")
                        .replace(".", "")
                        .replace(",", ".");
                double price = Double.parseDouble(priceStr);
                Element link = articleElement.select("a").first();
                String postUrl = link != null ? this.getUrl() + link.attr("href") : "";
                String imageUrl = articleElement.select("img[src]").attr("src");

                if (normalizeString(name).contains(normalizeString(productName))) {
                    GenericElementFravega genericElement = new GenericElementFravega(name, postUrl, price, imageUrl);
                    genericElementsMap.put(name, genericElement);
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

    public String generateJson(List<GenericElementFravega> genericElements) {
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
class GenericElementFravega {
    private String name;
    private String postUrl;
    private double price;
    private String productImageUrl;
}