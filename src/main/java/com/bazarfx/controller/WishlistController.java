package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.Product;
import com.bazarfx.util.SessionManager;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Wishlist screen - the current user's saved listings, rendered as the same
 * card-grid style as Browse. Backed by WishlistService (wishlist.json),
 * so saved items survive across logins.
 */
public class WishlistController {

    @FXML private Label resultsCountLabel;
    @FXML private Label emptyStateLabel;
    @FXML private FlowPane productGrid;

    private MainController mainController;

    private static final double CARD_WIDTH = 190.0;
    private static final double IMAGE_HEIGHT = 140.0;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        refresh();
    }

    private void refresh() {
        String me = SessionManager.getCurrentUser().getUsername();
        List<String> savedIds = AppContext.get().wishlistService.getProductIdsForUser(me);

        List<Product> products = new ArrayList<>();
        for (String id : savedIds) {
            Product p = AppContext.get().productService.getById(id);
            if (p != null) products.add(p);
        }

        productGrid.getChildren().clear();
        for (Product product : products) {
            productGrid.getChildren().add(createProductCard(product));
        }

        resultsCountLabel.setText(products.size() + (products.size() == 1 ? " saved listing" : " saved listings"));
        emptyStateLabel.setVisible(products.isEmpty());
        emptyStateLabel.setManaged(products.isEmpty());
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(6.0);
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        card.getStyleClass().add("product-card");

        card.getChildren().add(buildThumbnail(product));

        Label priceLabel = new Label("BDT " + formatPrice(product.getPrice()));
        priceLabel.getStyleClass().add("card-price");

        Label titleLabel = new Label(product.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxHeight(38.0);

        Label locationLabel = new Label("\uD83D\uDCCD " + safe(product.getLocation()));
        locationLabel.getStyleClass().add("card-meta");

        HBox badgeRow = new HBox(6.0);
        badgeRow.setAlignment(Pos.CENTER_LEFT);
        Label conditionBadge = new Label(safe(product.getCondition()));
        conditionBadge.getStyleClass().add(
                "New".equalsIgnoreCase(product.getCondition()) ? "badge-new" : "badge-used");
        Label categoryBadge = new Label(safe(product.getCategory()));
        categoryBadge.getStyleClass().add("badge-category");
        badgeRow.getChildren().addAll(conditionBadge, categoryBadge);

        card.getChildren().addAll(priceLabel, titleLabel, badgeRow, locationLabel);
        card.setOnMouseClicked(e -> {
            if (mainController != null) mainController.openProductDetail(product.getId());
        });

        return card;
    }

    /** Thumbnail with a filled heart button overlay - clicking it removes the item from the wishlist and refreshes the grid. */
    private StackPane buildThumbnail(Product product) {
        StackPane frame = new StackPane();
        frame.setPrefSize(CARD_WIDTH, IMAGE_HEIGHT);
        frame.setMaxSize(CARD_WIDTH, IMAGE_HEIGHT);
        frame.getStyleClass().add("card-image-frame");

        List<String> imagePaths = product.getImagePaths();
        boolean hasPhoto = false;
        if (imagePaths != null && !imagePaths.isEmpty()) {
            File file = new File(imagePaths.get(0));
            if (file.exists()) {
                ImageView imageView = new ImageView(
                        new Image(file.toURI().toString(), CARD_WIDTH, IMAGE_HEIGHT, false, true));
                imageView.setFitWidth(CARD_WIDTH);
                imageView.setFitHeight(IMAGE_HEIGHT);
                frame.getChildren().add(imageView);
                hasPhoto = true;
            }
        }
        if (!hasPhoto) {
            Label placeholder = new Label(categoryIcon(product.getCategory()));
            placeholder.getStyleClass().add("card-image-placeholder");
            frame.getChildren().add(placeholder);
        }

        Button removeHeart = new Button("\u2665");
        removeHeart.getStyleClass().add("wishlist-heart-button-active");
        StackPane.setAlignment(removeHeart, Pos.TOP_RIGHT);
        removeHeart.setOnAction(e -> {
            String me = SessionManager.getCurrentUser().getUsername();
            AppContext.get().wishlistService.remove(me, product.getId());
            refresh();
        });
        frame.getChildren().add(removeHeart);

        return frame;
    }

    private String categoryIcon(String category) {
        if (category == null) return "\uD83C\uDFF7";
        return switch (category) {
            case "Electronics" -> "\uD83D\uDCBB";
            case "Vehicles" -> "\uD83D\uDE97";
            case "Furniture" -> "\uD83D\uDECB";
            case "Fashion" -> "\uD83D\uDC55";
            case "Books" -> "\uD83D\uDCDA";
            default -> "\uD83C\uDFF7";
        };
    }

    private String formatPrice(double price) {
        if (price == Math.floor(price)) {
            return String.format("%,.0f", price);
        }
        return String.format("%,.2f", price);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
