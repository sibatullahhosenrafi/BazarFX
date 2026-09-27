package com.bazarfx;

import com.bazarfx.api.ExchangeRateClient;
import com.bazarfx.concurrency.ConversationPoller;
import com.bazarfx.concurrency.ImageProcessor;
import com.bazarfx.concurrency.OrderStatusSimulator;
import com.bazarfx.concurrency.ReportGenerator;
import com.bazarfx.concurrency.UnreadMessagePoller;
import com.bazarfx.model.CartItem;
import com.bazarfx.service.AuthService;
import com.bazarfx.service.MessageService;
import com.bazarfx.service.OrderService;
import com.bazarfx.service.ProductService;
import com.bazarfx.service.ReviewService;
import com.bazarfx.service.WishlistService;

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
    public final WishlistService wishlistService = new WishlistService();
    public final MessageService messageService = new MessageService();

    public final ImageProcessor imageProcessor = new ImageProcessor();
    public final OrderStatusSimulator orderStatusSimulator = new OrderStatusSimulator(orderService);
    public final ReportGenerator reportGenerator = new ReportGenerator();
    // Week 7: JSON Parsing and API Response Handling - live BDT -> USD conversion.
    public final ExchangeRateClient exchangeRateClient = new ExchangeRateClient();

    // In-app buyer/seller chat concurrency:
    //  - conversationPoller re-queries whichever single chat thread is open.
    //  - unreadMessagePoller is an always-on background badge count for the
    //    sidebar, independent of which screen the user is currently on.
    public final ConversationPoller conversationPoller = new ConversationPoller();
    public final UnreadMessagePoller unreadMessagePoller = new UnreadMessagePoller(messageService);

    // Session-only cart - kept in memory for the currently logged-in user.
    public final List<CartItem> cart = new ArrayList<>();

    private AppContext() {
        com.bazarfx.seed.DemoDataSeeder.seedIfEmpty(this);
    }
    public static AppContext get() {
        return INSTANCE;
    }

    public void shutdown() {
        imageProcessor.shutdown();
        orderStatusSimulator.shutdown();
        reportGenerator.shutdown();
        conversationPoller.shutdown();
        unreadMessagePoller.shutdown();
    }
}
