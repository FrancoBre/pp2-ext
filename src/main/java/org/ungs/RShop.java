package org.ungs;

import entities.Shop;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RShop extends Shop {

    private final String directoryPath = "src/test/resources/US6/refresh/";

    public RShop() {
        super();
        this.name = "R";
    }

    @Override
    public Map<String, BigDecimal> search(String productName) {
        try {
            launchAsyncTaskToPollFiles(productName);
        } catch (Exception e) {
            System.out.println("Error while polling files: " + e.getMessage());
        }

        return Collections.emptyMap();
    }

    private void launchAsyncTaskToPollFiles(String productName) {
        Runnable filePollingTask = () -> {
            try {
                startFilePolling(productName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

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
                        this.addProduct();
                        Path fileName = (Path) event.context();
                        System.out.println("New file created: " + fileName);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        this.addProduct();
                        Path fileName = (Path) event.context();
                        System.out.println("File modified: " + fileName);
                    }
                }

                key.reset();
            }
        }
    }

    private Map<String, BigDecimal> addProduct() {
        Map<String, BigDecimal> product = new HashMap<>();
        product.put("a", new BigDecimal(100));

        this.notifySearchResult(product);
        return product;
    }

}
