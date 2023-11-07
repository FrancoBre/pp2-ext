package org.ungs;

import entities.Product;
import entities.ProductPresentation;
import entities.Shop;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RefreshableShop extends Shop {

    private Long initialPrice = 100L;
    private final String directoryPath = "plugins/";

    public RefreshableShop() {
        super();
        this.name = "refreshable";
    }

    @Override
    public Set<Product> search(String productName) {
        if (productName.isEmpty() || productName.equals("e")) {
            this.notifySearchResult(Collections.emptySet());
            return Collections.emptySet();
        }

        Set<Product> products = addProducts(productName);
        this.notifySearchResult(products);

        try {
            launchAsyncTaskToPollFiles(productName);
        } catch (Exception e) {
            System.out.println("Error while polling files: " + e.getMessage());
        }
        return products;
    }

    private Set<Product> addProducts(String productName) {
        Set<Product> products = new HashSet<>();

        ProductPresentation productPresentation = new ProductPresentation(initialPrice, "https://example.com/",
            "https://example.com/");
        Product product = new Product(productName, this.name, productPresentation);
        products.add(product);

        initialPrice += 100L;
        this.notifySearchResult(products);
        return products;
    }

    private void launchAsyncTaskToPollFiles(String productName) {
        Runnable filePollingTask = () -> {
            try {
                startFilePolling(productName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // Crea un ExecutorService para ejecutar el hilo de escucha en segundo plano
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(filePollingTask);
    }

    private void startFilePolling(String productName) throws Exception {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            Path path = Paths.get(directoryPath);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        this.addProducts(productName);
                        Path fileName = (Path) event.context();
                        System.out.println("New file created: " + fileName);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        this.addProducts(productName);
                        Path fileName = (Path) event.context();
                        System.out.println("File modified: " + fileName);
                    }
                }

                key.reset();
            }
        }
    }

}
