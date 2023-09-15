package org.ungs;

import shoppinator.core.interfaces.Scraper;

public class CScraper implements Scraper {

    @Override
    public String scrap(String productName) {
        return "[]";
    }

}