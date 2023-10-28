package org.ungs;

import entities.Product;
import entities.ProductPresentation;
import entities.Shop;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GShop extends Shop {

    final String name = "G";

    @Override
    public Set<Product> search(String productName) {
        if (productName.isEmpty() || productName.equals("e")) {
            return Collections.emptySet();
        }

        return this.getProducts(productName);
    }

    private Set<Product> getProducts(String productName) {
        Set<Product> products = new HashSet<>();

        ProductPresentation productPresentation = new ProductPresentation(200L, "https://example.com/",
            "https://example.com/");
        Product product = new Product(productName, this.name, productPresentation);
        products.add(product);

        return products;
    }
}
