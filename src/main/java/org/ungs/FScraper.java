package org.ungs;

import shoppinator.core.interfaces.Scraper;

public class FScraper implements Scraper {

    @Override
    public String scrap(String productName) {
        if(productName.isEmpty() || productName.equals("e")) {
            return "[]";
        }
        if (productName.equals("a")) {
            return "[{\"name\":\"a\",\"post_url\":\"https://example.com/\",\"product_presentation\":{\"price\":799.99,\"product_image_url\":\"https://example.com/\"}}]";
        }
        if (productName.equals("b")) {
            return "[{\"name\":\"b\",\"post_url\":\"https://example.com/\",\"product_presentation\":{\"price\":1299.99,\"product_image_url\":\"https://example.com/\"}}]";
        }

        throw new IllegalArgumentException();
    }

}
