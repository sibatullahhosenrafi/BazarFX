package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

/**
 * Browse screen - shows active listings as a wrapping grid of image cards
 * (Bikroy/Facebook-Marketplace style) instead of a plain data table.
 */
public class BrowseController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Label resultsCountLabel;
    @FXML private FlowPane productGrid;

    private MainController mainController;

    private static final double CARD_WIDTH = 190.0;
    private static final double IMAGE_HEIGHT = 140.0;

    @FXML
    private void initialize() {
        categoryFilter.setItems(FXCollections.observableArrayList(
                "All", "Electronics", "Vehicles", "Furniture", "Fashion", "Books", "Other"));
        categoryFilter.setValue("All");

        refresh();
    }

    /** Called by MainController right after this fragment is loaded, so a card click can navigate. */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onSearch() {
        String keyword = searchField.getText();
        String category = categoryFilter.getValue();
        renderProducts(AppContext.get().productService.search(keyword, category));
    }

    private void refresh() {
        renderProducts(AppContext.get().productService.getAllActive());
    }

    private void renderProducts(List<Product> products) {
        productGrid.getChildren().clear();
        resultsCountLabel.setText(products.size() + (products.size() == 1 ? " listing found" : " listings found"));
        for (Product product : products) {
            productGrid.getChildren().add(createProductCard(product));
        }
    }

    /** Builds one clickable "ad card": photo (or placeholder) on top, price/title/meta below. */
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
        card.setOnMouseClicked(e -> openSelected(product));

        return card;
    }

    /** Real photo if the listing has one on disk, otherwise a category-icon placeholder tile. */
    private StackPane buildThumbnail(Product product) {
        StackPane frame = new StackPane();
        frame.setPrefSize(CARD_WIDTH, IMAGE_HEIGHT);
        frame.setMaxSize(CARD_WIDTH, IMAGE_HEIGHT);
        frame.getStyleClass().add("card-image-frame");

        List<String> imagePaths = product.getImagePaths();
        if (imagePaths != null && !imagePaths.isEmpty()) {
            File file = new File(imagePaths.get(0));
            if (file.exists()) {
                ImageView imageView = new ImageView(
                        new Image(file.toURI().toString(), CARD_WIDTH, IMAGE_HEIGHT, false, true));
                imageView.setFitWidth(CARD_WIDTH);
                imageView.setFitHeight(IMAGE_HEIGHT);
                frame.getChildren().add(imageView);
                return frame;
            }
        }

        Label placeholder = new Label(categoryIcon(product.getCategory()));
        placeholder.getStyleClass().add("card-image-placeholder");
        frame.getChildren().add(placeholder);
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

    private void openSelected(Product product) {
        if (mainController != null) {
            mainController.openProductDetail(product.getId());
        }
    }
}
