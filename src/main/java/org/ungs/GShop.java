package org.ungs;

import entities.Shop;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GShop extends Shop {

    public GShop() {
        super();
        this.name = "G";
    }

    @Override
    public Set<Map<String, BigDecimal>> search(String productName) {
        if (productName.isEmpty() || productName.equals("e")) {
            this.notifySearchResult(Collections.emptySet());
            return Collections.emptySet();
        }

        Set<Map<String, BigDecimal>> products = this.getProducts(productName);
        this.notifySearchResult(products);
        return products;
    }

    private Set<Map<String, BigDecimal>> getProducts(String productName) {
        Set<Map<String, BigDecimal>> products = new HashSet<>();
        Map<String, BigDecimal> product = new HashMap<>();
        product.put(productName, new BigDecimal(200));
        products.add(product);

        return products;
    }
}
