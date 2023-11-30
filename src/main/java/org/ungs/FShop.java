package org.ungs;

import entities.Shop;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FShop implements Shop {

    @Override
    public Map<String, BigDecimal> search(String productName) {
        if (!productName.contains("a")) {
            return Collections.emptyMap();
        }

        return this.getProduct(productName);
    }

    @Override
    public String getName() {
        return "F";
    }

    private Map<String, BigDecimal> getProduct(String productName) {
        Map<String, BigDecimal> product = new HashMap<>();
        product.put(productName, new BigDecimal(100));

        return product;
    }
}
