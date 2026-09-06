package com.bazarfx;

import com.bazarfx.concurrency.ImageProcessor;
import com.bazarfx.concurrency.OrderStatusSimulator;
import com.bazarfx.concurrency.ReportGenerator;
import com.bazarfx.model.CartItem;
import com.bazarfx.service.AuthService;
import com.bazarfx.service.OrderService;
import com.bazarfx.service.ProductService;
import com.bazarfx.service.ReviewService;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple app-wide singleton holder so every controller shares the same
 * service instances (and therefore the same in-memory data) instead of
 * each screen re-loading its own copy of products.json etc.
 */
public class AppContext {

    private static final AppContext INSTANCE = new AppContext();

    public final AuthService authService = new AuthService();
    public final ProductService productService = new ProductService();
    public final OrderService orderService = new OrderService();
    public final ReviewService reviewService = new ReviewService();

    public final ImageProcessor imageProcessor = new ImageProcessor();
    public final OrderStatusSimulator orderStatusSimulator = new OrderStatusSimulator(orderService);
    public final ReportGenerator reportGenerator = new ReportGenerator();

    // Session-only cart/wishlist - kept in memory for the currently logged-in user.
    public final List<CartItem> cart = new ArrayList<>();
    public final List<String> wishlistProductIds = new ArrayList<>();

    private AppContext() {
    }

    public static AppContext get() {
        return INSTANCE;
    }

    public void shutdown() {
        imageProcessor.shutdown();
        orderStatusSimulator.shutdown();
        reportGenerator.shutdown();
    }
}
