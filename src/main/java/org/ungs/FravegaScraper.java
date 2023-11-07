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

public class FravegaScraper extends Shop {

    String shopUrl = "https://www.fravega.com";
    //String shopUrl = "src/resources/fravega-mouse.html";

    public FravegaScraper() {}

    @Override
    public Set<Product> search(String productName) {

        if (productName.isEmpty()) {
            return new HashSet<>();
        }

        String currentUrlSearch = shopUrl;

        if (shopUrl.contains("www.")){
            currentUrlSearch = shopUrl+"/l/?keyword=" + productName.replace(" ","+")+"&sorting=LOWEST_SALE_PRICE&page=";
        }

        Set<Callable<Set<Product>>> tasks = new HashSet<>();

        int pageNum = 1;
        boolean productsExists = true;
        boolean productsHtmlIsOffline = false;

        while(productsExists && !productsHtmlIsOffline) {//Recorremos cada una de las paginas de la tienda
            String urlIt = (currentUrlSearch + pageNum);

            if (!currentUrlSearch.contains("www.")){
                urlIt = currentUrlSearch;
                productsHtmlIsOffline = true;
            }

            Set<Product> products = scrapeProductsFromPage(urlIt, productName);
            pageNum++;
            productsExists = !products.isEmpty();//Si llego al final de las paginas, salimos
            tasks.add(() -> products);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Set<Future<Set<Product>>> futures;

        try {
            futures = new HashSet<>(executorService.invokeAll(tasks));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            executorService.shutdown();
        }

        Set<Product> productsList = new HashSet<>();
        for (Future<Set<Product>> future : futures) {
            try {
                productsList.addAll(future.get());
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }

        return productsList;
    }

    private Set<Product>  scrapeProductsFromPage(String urlSearch, String productName) {
        Set<Product> products = new HashSet<>();

        try {
            Document documentHtml = getDocumentHtml(urlSearch);
            Elements articleElements = documentHtml.select("article.sc-ef269aa1-2.FmCUT");

            for (Element articleElement : articleElements) {
                String name = articleElement.select("span.sc-6321a7c8-0.jKvHol").text();

                String priceStr = articleElement.select("div.sc-854e1b3a-0.kfAWhD span.sc-ad64037f-0.ixxpWu").text()
                        .replace("$", "")
                        .replace(".", "")
                        .replace(",", ".");

                String priceWithoutDecimals[] = priceStr.split("\\.");//supr decimales
                Long price = Long.parseLong(priceWithoutDecimals[0]);

                Element link = articleElement.select("a").first();
                String postUrl = link != null ? shopUrl + link.attr("href") : "";
                String imageUrl = articleElement.select("img[src]").attr("src");

                if (normalizeString(name).contains(normalizeString(productName))) {
                    Product product = new Product(name, this.getName(), new ProductPresentation(price, postUrl, imageUrl));
                    products.add(product);
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