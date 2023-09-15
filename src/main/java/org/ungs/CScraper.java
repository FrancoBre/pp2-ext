package org.ungs;

import shoppinator.core.interfaces.Scraper;

public class CScraper implements Scraper {

    @Override
    public String scrap(String productName) {
        if (productName.equals("e")) {
            return "[{\"name\":\"e\",\"post_url\":\"https://example.com/\",\"product_presentation\":{\"price\":799.99,\"product_image_url\":\"https://example.com/\"}}]";
        }

        return "[]";
    }

}