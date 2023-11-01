
import entities.Result;
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
    public Set<Result> search(String productName) {
        if (productName.isEmpty() || productName.equals("e")) {
            this.notifySearchResult(Collections.emptySet());
            return Collections.emptySet();
        }

        Set<Result> products = this.getProducts(productName);
        this.notifySearchResult(products);
        return products;
    }

    private Set<Result> getProducts(String productName) {
        Set<Result> products = new HashSet<>();

        Result product = null;
        product.setPrice(50L);
        product.setName("productMock");
        product.setPostUrl("https://example.com/");
        products.add(product);
        Result product2 = null;
        product2.setPrice(90L);
        product2.setName("otherProductMock");
        product2.setPostUrl("https://example.com/");

        return products;
    }
}
