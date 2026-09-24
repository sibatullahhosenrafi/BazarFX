package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.CartItem;
import com.bazarfx.model.Order;
import com.bazarfx.model.Product;
import com.bazarfx.concurrency.NotificationService;
import com.bazarfx.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class CheckoutController {

    @FXML private Label totalLabel;
    @FXML private ListView<String> orderStatusList;
    @FXML private Label confirmationLabel;

    private final List<Order> placedOrders = new ArrayList<>();
    private Timeline statusPoller;

    @FXML
    private void initialize() {
        double total = 0;
        for (CartItem item : AppContext.get().cart) {
            Product p = AppContext.get().productService.getById(item.getProductId());
            if (p != null) total += p.getPrice() * item.getQuantity();
        }
        totalLabel.setText("Order total: BDT " + total);
    }

    @FXML
    private void onConfirmPurchase() {
        String buyer = SessionManager.getCurrentUser().getUsername();

        for (CartItem item : AppContext.get().cart) {
            Product p = AppContext.get().productService.getById(item.getProductId());
            if (p == null) continue;
            double lineTotal = p.getPrice() * item.getQuantity();
            Order order = AppContext.get().orderService.placeOrder(
                    buyer, p.getId(), p.getTitle(), item.getQuantity(), lineTotal);
            placedOrders.add(order);
            // Kick off the background pipeline that advances this order's status over time.
            AppContext.get().orderStatusSimulator.simulate(order);
        }

        AppContext.get().cart.clear();
        confirmationLabel.setText("Purchase confirmed! Watch your order status update live below.");
        NotificationService.getInstance().push("Order placed. " + placedOrders.size() + " item(s) on the way.");

        startStatusPolling();
    }

    /**
     * Polls the shared, thread-safe Order objects once a second on the JavaFX
     * Application Thread and refreshes the list - the UI-side half of the
     * background OrderStatusSimulator.
     */
    private void startStatusPolling() {
        statusPoller = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            List<String> lines = new ArrayList<>();
            for (Order o : placedOrders) {
                lines.add(o.getProductTitle() + "  -  " + o.getStatus());
            }
            orderStatusList.setItems(FXCollections.observableArrayList(lines));
        }));
        statusPoller.setCycleCount(Timeline.INDEFINITE);
        statusPoller.play();
    }
}
