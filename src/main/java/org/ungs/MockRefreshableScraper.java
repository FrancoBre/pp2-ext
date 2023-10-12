package org.ungs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import shoppinator.core.interfaces.Scraper;

public class MockRefreshableScraper extends Scraper {

    public String scrap(String productName) {
        List<GenericElement> mockElementList = generateMockElementList();
        return this.toJson(mockElementList);
    }

    private List<GenericElement> generateMockElementList() {
        List<GenericElement> genericElements = new java.util.ArrayList<>();
        int randomSize = (int) (Math.random() * 10.0D);
        int i = 0;

        while (i < randomSize) {
            GenericElement genericElement = new GenericElement();
            genericElement.setName(this.generateRandomString());
            genericElement.setPostUrl("https://example.com/");
            genericElement.setPrice(this.generateRandomDouble());
            genericElement.setProductImageUrl("https://example.com/");
            genericElements.add(genericElement);
            ++i;
        }

        return genericElements;
    }

    private double generateRandomDouble() {
        return Math.random() * 1000.0D;
    }

    private String generateRandomString() {
        String randomString = "";
        int randomSize = (int) (Math.random() * 10.0D);
        int i = 0;

        while (i < randomSize) {
            randomString = randomString + (char) (int) (Math.random() * 26.0D + 97.0D);
            ++i;
        }

        return randomString;
    }

    private String toJson(List<GenericElement> genericElements) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(genericElements);
        } catch (JsonProcessingException var4) {
            var4.printStackTrace();
            return "";
        }
    }

    private class GenericElement {

        private String name;
        private String postUrl;
        private double price;
        private String productImageUrl;

        public String getName() {
            return this.name;
        }

        public String getPostUrl() {
            return this.postUrl;
        }

        public double getPrice() {
            return this.price;
        }

        public String getProductImageUrl() {
            return this.productImageUrl;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPostUrl(String postUrl) {
            this.postUrl = postUrl;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public void setProductImageUrl(String productImageUrl) {
            this.productImageUrl = productImageUrl;
        }
    }
}
