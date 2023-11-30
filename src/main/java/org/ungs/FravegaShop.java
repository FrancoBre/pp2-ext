package org.ungs;

import entities.Shop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FravegaShop extends Shop {

    //String shopUrl = "https://www.fravega.com";
    String shopUrl = "src/test/resources/US3/fravega-mouse-teclado-webcam.html";

    public FravegaShop() {
        super();
        this.name = "Fravega";
    }

    @Override
    public Map<String, BigDecimal> search(String productName) {
        if (productName.isEmpty()) {
            return new HashMap<>();
        }

        String currentUrlSearch = shopUrl;

        if (isValidWebUrl(shopUrl)) {
            currentUrlSearch = shopUrl + "/l/?keyword=" + productName.replace(" ", "+") + "&sorting=LOWEST_SALE_PRICE&page=";
        }

        int pageNum = 1;
        boolean productsExists = true;
        boolean productsHtmlIsOffline = false;

        while (productsExists && !productsHtmlIsOffline) {
            String urlIt = (currentUrlSearch + pageNum);

            if (!isValidWebUrl(currentUrlSearch)) {
                urlIt = currentUrlSearch;
                productsHtmlIsOffline = true;
            }

            Map<String, BigDecimal> product = scrapeProductFromPage(urlIt, productName);
            pageNum++;
            productsExists = product != null && !product.isEmpty();

            if (product != null) {
                return product;
            }
        }

        return new HashMap<>();
    }

    private Map<String, BigDecimal> scrapeProductFromPage(String urlSearch, String productName) {
        try {
            Document documentHtml = getDocumentHtml(urlSearch);
            Elements articleElements = documentHtml.select("article.sc-ef269aa1-2.FmCUT");

            for (Element articleElement : articleElements) {
                String name = articleElement.select("span.sc-6321a7c8-0.jKvHol").text();

                String priceStr = articleElement.select("div.sc-854e1b3a-0.kfAWhD span.sc-ad64037f-0.ixxpWu").text()
                    .replace("$", "")
                    .replace(".", "")
                    .replace(",", ".");

                double price = Double.parseDouble(priceStr);
                BigDecimal priceBd = new BigDecimal(Double.toString(price));

                if (normalizeString(name).contains(normalizeString(productName))) {
                    Map<String, BigDecimal> articleMap = new HashMap<>();
                    articleMap.put(name, priceBd);

                    return articleMap;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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