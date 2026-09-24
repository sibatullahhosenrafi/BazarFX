package com.bazarfx.concurrency;

import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Copies uploaded photos into /data/images/{productId}/ on a background thread pool
 * so the Sell-a-Product screen never freezes while files are handled.
 */
public class ImageProcessor {

    // Fixed thread pool: bounded so many simultaneous uploads can't overwhelm the disk.
    private final ExecutorService pool = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "image-worker");
        t.setDaemon(true);
        return t;
    });

    /**
     * @param onEachSaved called on the JavaFX thread once per successfully copied image,
     *                     with the relative path to store on the Product.
     * @param onAllDone   called on the JavaFX thread once every submitted file has finished.
     */
    public void processImages(String productId, List<File> sourceFiles,
                               Consumer<String> onEachSaved, Runnable onAllDone) {

        java.util.concurrent.atomic.AtomicInteger remaining = new java.util.concurrent.atomic.AtomicInteger(sourceFiles.size());
        if (sourceFiles.isEmpty()) {
            onAllDone.run();
            return;
        }

        for (File file : sourceFiles) {
            pool.submit(() -> {
                try {
                    Path targetDir = Path.of("data", "images", productId);
                    Files.createDirectories(targetDir);
                    // Simulate the kind of work real resizing/compression would take.
                    Thread.sleep(300);
                    Path target = targetDir.resolve(file.getName());
                    Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

                    String relativePath = target.toString();
                    Platform.runLater(() -> onEachSaved.accept(relativePath));
                } catch (IOException | InterruptedException e) {
                    System.err.println("Image processing failed for " + file.getName() + ": " + e.getMessage());
                } finally {
                    if (remaining.decrementAndGet() == 0) {
                        Platform.runLater(onAllDone);
                    }
                }
            });
        }
    }

    public void shutdown() {
        pool.shutdown();
    }
}
