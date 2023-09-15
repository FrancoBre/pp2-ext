package org.ungs;

import shoppinator.core.interfaces.Scraper;

public class MScraper implements Scraper {

    @Override
    public String scrap(String productName) {
        if (productName.isEmpty() || productName.equals("e")) {
            return "[]";
        }
        if (productName.equals("a")) {
            return "[{\"name\":\"a\",\"post_url\":\"https://example.com/\",\"product_presentation\":{\"price\":799.99,\"product_image_url\":\"https://example.com/\"}}]";
        }
        if (productName.equals("featured")) {
            return "[{\"name\":\"i\",\"post_url\":\"https://example.com/\",\"product_presentation\":{\"price\":799.99,\"product_image_url\":\"https://example.com/\"}},"
                + "{\"name\":\"j\",\"post_url\":\"https://example.com/\",\"product_presentation\":{\"price\":799.99,\"product_image_url\":\"https://example.com/\"}}]";
        }

        return "[{\"name\":\"a\",\"post_url\":\"https://example.com/\",\"product_presentation\":{\"price\":799.99,\"product_image_url\":\"https://example.com/\"}},"
            + "{\"name\":\"b\",\"post_url\":\"https://example.com/\",\"product_presentation\":{\"price\":1299.99,\"product_image_url\":\"https://example.com/\"}}]";

    }

}
