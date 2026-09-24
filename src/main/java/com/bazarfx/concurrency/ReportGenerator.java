package com.bazarfx.concurrency;

import com.bazarfx.model.Order;
import com.bazarfx.model.Product;
import com.bazarfx.service.OrderService;
import com.bazarfx.service.ProductService;
import javafx.application.Platform;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Builds the seller/marketplace report by splitting independent aggregation
 * work across an ExecutorService and combining the results with Future.get().
 * This is the project's demonstration of Callable + Future.
 */
public class ReportGenerator {

    public static class ReportResult {
        public double totalRevenue;
        public String topCategory;
        public String mostActiveSeller;
        public int totalOrders;
    }

    private final ExecutorService pool = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "report-worker");
        t.setDaemon(true);
        return t;
    });

    public void generateAsync(ProductService productService, OrderService orderService,
                               Consumer<ReportResult> onComplete) {

        List<Order> orders = orderService.getAllSnapshot();
        List<Product> products = productService.getAllSnapshot();

        Callable<Double> revenueTask = () -> {
            double sum = 0;
            for (Order o : orders) sum += o.getTotalPrice();
            simulateWork();
            return sum;
        };

        Callable<String> categoryTask = () -> {
            simulateWork();
            Map<String, Long> counts = products.stream()
                    .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
            return counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
        };

        Callable<String> sellerTask = () -> {
            simulateWork();
            Map<String, Long> counts = products.stream()
                    .collect(Collectors.groupingBy(Product::getSellerUsername, Collectors.counting()));
            return counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");
        };

        Future<Double> revenueFuture = pool.submit(revenueTask);
        Future<String> categoryFuture = pool.submit(categoryTask);
        Future<String> sellerFuture = pool.submit(sellerTask);

        // Combine the futures on a separate thread so the calling (UI) thread is never blocked.
        pool.submit(() -> {
            ReportResult result = new ReportResult();
            try {
                result.totalRevenue = revenueFuture.get();
                result.topCategory = categoryFuture.get();
                result.mostActiveSeller = sellerFuture.get();
                result.totalOrders = orders.size();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Report generation failed: " + e.getMessage());
            }
            Platform.runLater(() -> onComplete.accept(result));
        });
    }

    private void simulateWork() {
        try {
            Thread.sleep(600); // stands in for real aggregation over a larger dataset
        } catch (InterruptedException ignored) {
        }
    }

    public void shutdown() {
        pool.shutdown();
    }
}
