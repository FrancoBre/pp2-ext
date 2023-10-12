package org.ungs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import shoppinator.core.interfaces.Scraper;

public class MockRefreshableScraper extends Scraper {

    private List<GenericElement> elementList;
    private int callCounter;

    public MockRefreshableScraper() {
        this.elementList = new ArrayList<>();
        this.callCounter = 0;
    }

    @Override
    public String scrap(String productName) {
        callCounter++;
        updateElementList();
        return toJson();
    }

    private void updateElementList() {
        elementList.clear();
        if (callCounter % 2 == 0) {
            elementList.add(new GenericElement("a", 10L));
            elementList.add(new GenericElement("b", 100L));
            elementList.add(new GenericElement("c", 1000L));
            elementList.add(new GenericElement("d", 10000L));
        } else {
            elementList.add(new GenericElement("a", 10L));
            elementList.add(new GenericElement("b", 100L));
            elementList.add(new GenericElement("c", 1000L));
        }
    }

    private String toJson() {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(this.elementList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private class GenericElement {

        private String name;
        private String postUrl;
        private Long price;
        private String productImageUrl;

        public GenericElement(String name, Long price) {
            this.name = name;
            this.postUrl = "https://www.example.com";
            this.productImageUrl = "https://www.example.com";
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public String getPostUrl() {
            return postUrl;
        }

        public Long getPrice() {
            return price;
        }

        public String getProductImageUrl() {
            return productImageUrl;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPostUrl(String postUrl) {
            this.postUrl = postUrl;
        }

        public void setPrice(Long price) {
            this.price = price;
        }

        public void setProductImageUrl(String productImageUrl) {
            this.productImageUrl = productImageUrl;
        }
    }
}
