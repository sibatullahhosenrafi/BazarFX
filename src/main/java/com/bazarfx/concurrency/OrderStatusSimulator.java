package com.bazarfx.concurrency;

import com.bazarfx.model.Order;
import com.bazarfx.model.OrderStatus;
import com.bazarfx.service.OrderService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Advances a placed order through PENDING -> CONFIRMED -> SHIPPED -> DELIVERED
 * automatically, purely for demo purposes, using a ScheduledExecutorService.
 * This is the project's clearest example of "concurrent task management".
 */
public class OrderStatusSimulator {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "order-status-simulator");
        t.setDaemon(true);
        return t;
    });

    private final OrderService orderService;

    public OrderStatusSimulator(OrderService orderService) {
        this.orderService = orderService;
    }

    public void simulate(Order order) {
        OrderStatus[] pipeline = { OrderStatus.CONFIRMED, OrderStatus.SHIPPED, OrderStatus.DELIVERED };
        long delaySeconds = 4;

        for (OrderStatus nextStatus : pipeline) {
            scheduler.schedule(() -> {
                order.setStatus(nextStatus);
                orderService.persist();
                NotificationService.getInstance().push(
                        "Order " + order.getId() + " is now " + nextStatus + ".");
            }, delaySeconds, TimeUnit.SECONDS);
            delaySeconds += 4;
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
