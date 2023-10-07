package org.ungs;

import shoppinator.core.interfaces.Scraper;

public class FravegaScraper extends Scraper {

    public FravegaScraper() {
    }

    @Override
    public String scrap(String productName) {
        if (productName.equals("featured")) {
            return "[{\"name\":\"i\",\"post_url\":\"https://example.com/\",\"price\":799.99,\"product_image_url\":\"https://example.com/\"},"
                + "{\"name\":\"j\",\"post_url\":\"https://example.com/\",\"price\":799.99,\"product_image_url\":\"https://example.com/\"}]";
        }

        return "[{\"name\":\"a\",\"post_url\":\"https://example.com/\",\"price\":799.99,\"product_image_url\":\"https://example.com/\"}]";

    }

}
