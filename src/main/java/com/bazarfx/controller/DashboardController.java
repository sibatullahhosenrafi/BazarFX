package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.concurrency.ReportGenerator;
import com.bazarfx.model.Order;
import com.bazarfx.model.OrderStatus;
import com.bazarfx.model.Product;
import com.bazarfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Seller dashboard, redesigned as a set of report cards (top stat row +
 * order-status distribution + listings + marketplace-wide insights) rather
 * than a flat list of labels - the same "reports at a glance" layout style
 * as the reference design the project owner asked for.
 */
public class DashboardController {

    @FXML private Label statRevenueLabel;
    @FXML private Label statOrdersLabel;
    @FXML private Label statListingsLabel;
    @FXML private Label statRatingLabel;

    @FXML private ProgressBar pendingBar;
    @FXML private ProgressBar confirmedBar;
    @FXML private ProgressBar shippedBar;
    @FXML private ProgressBar deliveredBar;
    @FXML private Label pendingCountLabel;
    @FXML private Label confirmedCountLabel;
    @FXML private Label shippedCountLabel;
    @FXML private Label deliveredCountLabel;

    @FXML private ListView<String> myListingsView;

    @FXML private ProgressIndicator reportProgress;
    @FXML private Label marketRevenueLabel;
    @FXML private Label marketCategoryLabel;
    @FXML private Label marketSellerLabel;
    @FXML private Label marketOrdersLabel;

    @FXML
    private void initialize() {
        reportProgress.setVisible(false);
        String me = SessionManager.getCurrentUser().getUsername();

        List<Product> myListings = AppContext.get().productService.getBySeller(me);
        Set<String> myProductIds = new HashSet<>();
        int totalViews = 0;
        var lines = new java.util.ArrayList<String>();
        for (Product p : myListings) {
            myProductIds.add(p.getId());
            totalViews += p.getViews();
            lines.add(p.getTitle() + "  -  " + p.getViews() + " views  -  BDT " + p.getPrice());
        }
        myListingsView.setItems(FXCollections.observableArrayList(lines));
        statListingsLabel.setText(myListings.size() + " (" + totalViews + " total views)");

        List<Order> myOrders = AppContext.get().orderService.getAllSnapshot().stream()
                .filter(o -> myProductIds.contains(o.getProductId()))
                .toList();

        double revenue = myOrders.stream().mapToDouble(Order::getTotalPrice).sum();
        statRevenueLabel.setText(String.format("BDT %,.2f", revenue));
        statOrdersLabel.setText(String.valueOf(myOrders.size()));

        double avgRating = AppContext.get().reviewService.getAverageRating(me);
        int reviewCount = AppContext.get().reviewService.getForSeller(me).size();
        statRatingLabel.setText(reviewCount == 0
                ? "No ratings yet"
                : String.format("%.1f / 5.0 (%d)", avgRating, reviewCount));

        renderStatusDistribution(myOrders);
    }

    /** Fills the four progress bars proportionally to whichever status has the most orders. */
    private void renderStatusDistribution(List<Order> myOrders) {
        long pending = countByStatus(myOrders, OrderStatus.PENDING);
        long confirmed = countByStatus(myOrders, OrderStatus.CONFIRMED);
        long shipped = countByStatus(myOrders, OrderStatus.SHIPPED);
        long delivered = countByStatus(myOrders, OrderStatus.DELIVERED);
        long max = Math.max(1, Math.max(Math.max(pending, confirmed), Math.max(shipped, delivered)));

        pendingBar.setProgress(pending / (double) max);
        confirmedBar.setProgress(confirmed / (double) max);
        shippedBar.setProgress(shipped / (double) max);
        deliveredBar.setProgress(delivered / (double) max);

        pendingCountLabel.setText(String.valueOf(pending));
        confirmedCountLabel.setText(String.valueOf(confirmed));
        shippedCountLabel.setText(String.valueOf(shipped));
        deliveredCountLabel.setText(String.valueOf(delivered));
    }

    private long countByStatus(List<Order> orders, OrderStatus status) {
        return orders.stream().filter(o -> o.getStatus() == status).count();
    }

    @FXML
    private void onGenerateReport() {
        reportProgress.setVisible(true);
        AppContext.get().reportGenerator.generateAsync(
                AppContext.get().productService,
                AppContext.get().orderService,
                (ReportGenerator.ReportResult result) -> {
                    reportProgress.setVisible(false);
                    marketRevenueLabel.setText("Total marketplace revenue: BDT " + result.totalRevenue);
                    marketCategoryLabel.setText("Most listed category: " + result.topCategory);
                    marketSellerLabel.setText("Most active seller: " + result.mostActiveSeller);
                    marketOrdersLabel.setText("Total orders placed: " + result.totalOrders);
                });
    }
}
