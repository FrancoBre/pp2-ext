package org.ungs;

import entities.Product;
import entities.ProductPresentation;
import entities.Shop;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FShop extends Shop {

    public FShop() {
        super();
        this.name = "F";
    }

    @Override
    public Set<Product> search(String productName) {
        if (productName.isEmpty() || productName.equals("e")) {
            return Collections.emptySet();
        }

        Set<Product> products = this.getProducts(productName);
        this.notifySearchResult(products);
        return products;
    }

    private Set<Product> getProducts(String productName) {
        Set<Product> products = new HashSet<>();

        ProductPresentation productPresentation = new ProductPresentation(100L, "https://example.com/",
            "https://example.com/");
        Product product = new Product(productName, this.name, productPresentation);
        products.add(product);

        return products;
    }
}
