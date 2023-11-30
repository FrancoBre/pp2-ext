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

public class NonRShop extends Shop {

    public NonRShop() {
        super();
        this.name = "R";
    }

    @Override
    public Map<String, BigDecimal> search(String productName) {
        return Collections.emptyMap();
    }

}
