package com.bazarfx.seed;

import com.bazarfx.AppContext;
import com.bazarfx.model.Order;
import com.bazarfx.model.OrderStatus;
import com.bazarfx.model.Product;
import com.bazarfx.model.Review;

public class DemoDataSeeder {

    public static void seedIfEmpty(AppContext ctx) {
        if (!ctx.productService.getAllSnapshot().isEmpty()) {
            return;
        }

        ctx.authService.signup("demo_seller", "seller@bazarfx.demo", "01711000000", "Dhaka", "demo1234");
        ctx.authService.signup("demo_buyer", "buyer@bazarfx.demo", "01722000000", "Chattogram", "demo1234");

        addProduct(ctx, "demo_seller", "iPhone 12, 128GB", "Barely used, no scratches, comes with original box and charger.", "Electronics", 42000, "Used", "Dhaka");
        addProduct(ctx, "demo_seller", "Dell Inspiron 15 Laptop", "Core i5, 8GB RAM, 512GB SSD. Great for students.", "Electronics", 38500, "Used", "Dhaka");
        addProduct(ctx, "demo_seller", "Yamaha FZS V3", "2021 model, 9,000 km driven, single owner, all papers updated.", "Vehicles", 210000, "Used", "Chattogram");
        addProduct(ctx, "demo_seller", "3-Seater Sofa Set", "Wooden frame, fabric cushions, very comfortable, minor wear.", "Furniture", 15000, "Used", "Sylhet");
        addProduct(ctx, "demo_buyer", "Brand New Study Table", "Compact study table, still in packaging, unused.", "Furniture", 4500, "New", "Dhaka");
        addProduct(ctx, "demo_buyer", "Denim Jacket (L)", "Worn twice, excellent condition, imported brand.", "Fashion", 1200, "Used", "Khulna");
        addProduct(ctx, "demo_seller", "HSC Physics & Chemistry Guide Books", "Complete set, no torn pages, some highlighting.", "Books", 650, "Used", "Rajshahi");
        addProduct(ctx, "demo_buyer", "Canon EOS 1500D DSLR", "Comes with 18-55mm lens, bag, and 32GB SD card.", "Electronics", 32000, "Used", "Dhaka");

        // Reviews reference a real order's id via a FOREIGN KEY (see DatabaseManager /
        // ReviewDao), so we place two delivered demo orders first and then attach a
        // review to each one - this is what actually exercises the orders<->reviews
        // relationship instead of a stray, unrelated id.
        Order order1 = ctx.orderService.placeOrder("demo_buyer", "seed-product-1", "iPhone 12, 128GB", 1, 42000);
        order1.setStatus(OrderStatus.DELIVERED);
        Order order2 = ctx.orderService.placeOrder("demo_buyer", "seed-product-2", "Dell Inspiron 15 Laptop", 1, 38500);
        order2.setStatus(OrderStatus.DELIVERED);
        ctx.orderService.persist();

        ctx.reviewService.addReview(new Review(order1.getId(), "demo_seller", "demo_buyer", 5,
                "Item exactly as described, smooth pickup. Highly recommended seller!"));
        ctx.reviewService.addReview(new Review(order2.getId(), "demo_seller", "demo_buyer", 4,
                "Good condition, slightly delayed reply but overall a fair deal."));
    }

    private static void addProduct(AppContext ctx, String seller, String title, String description,
                                   String category, double price, String condition, String location) {
        Product product = new Product(seller, title, description, category, price, condition, location);
        ctx.productService.addProduct(product);
    }
}

