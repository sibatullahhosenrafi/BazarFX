package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.Product;
import com.bazarfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Browse screen - shows active listings as a wrapping grid of image cards
 * (Bikroy/Facebook-Marketplace style) instead of a plain data table.
 *
 * Also hosts the search/filter/sort toolbar: keyword + category (original),
 * plus price range, minimum seller rating, and a sort order (new). Price and
 * keyword/category filtering happen in ProductService; minimum-rating
 * filtering and sorting happen here, since seller rating is looked up from
 * ReviewService, a second service ProductService intentionally doesn't
 * depend on.
 */
public class BrowseController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> minRatingFilter;
    @FXML private ComboBox<String> sortFilter;
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

        minRatingFilter.setItems(FXCollections.observableArrayList(
                "Any rating", "3+ stars", "4+ stars", "4.5+ stars"));
        minRatingFilter.setValue("Any rating");

        sortFilter.setItems(FXCollections.observableArrayList(
                "Relevance", "Price: Low to High", "Price: High to Low",
                "Newest First", "Most Viewed", "Top Rated Sellers"));
        sortFilter.setValue("Relevance");

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
        Double minPrice = parsePrice(minPriceField.getText());
        Double maxPrice = parsePrice(maxPriceField.getText());

        List<Product> results = AppContext.get().productService.search(keyword, category, minPrice, maxPrice);
        results = applyMinRating(results, ratingThreshold(minRatingFilter.getValue()));
        results = applySort(results, sortFilter.getValue());
        renderProducts(results);
    }

    @FXML
    private void onClearFilters() {
        searchField.clear();
        categoryFilter.setValue("All");
        minPriceField.clear();
        maxPriceField.clear();
        minRatingFilter.setValue("Any rating");
        sortFilter.setValue("Relevance");
        refresh();
    }

    private void refresh() {
        renderProducts(AppContext.get().productService.getAllActive());
    }

    /** Blank/invalid price text just means "no limit" rather than an error dialog - keeps the filter forgiving. */
    private Double parsePrice(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double ratingThreshold(String option) {
        if (option == null) return 0;
        return switch (option) {
            case "3+ stars" -> 3.0;
            case "4+ stars" -> 4.0;
            case "4.5+ stars" -> 4.5;
            default -> 0.0;
        };
    }

    private List<Product> applyMinRating(List<Product> products, double minRating) {
        if (minRating <= 0) return products;
        List<Product> filtered = new ArrayList<>();
        for (Product p : products) {
            if (AppContext.get().reviewService.getAverageRating(p.getSellerUsername()) >= minRating) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    private List<Product> applySort(List<Product> products, String sortOption) {
        List<Product> sorted = new ArrayList<>(products);
        if (sortOption == null) return sorted;
        switch (sortOption) {
            case "Price: Low to High" -> sorted.sort(Comparator.comparingDouble(Product::getPrice));
            case "Price: High to Low" -> sorted.sort(Comparator.comparingDouble(Product::getPrice).reversed());
            case "Newest First" -> sorted.sort(Comparator.comparing(Product::getCreatedAt).reversed());
            case "Most Viewed" -> sorted.sort(Comparator.comparingInt(Product::getViews).reversed());
            case "Top Rated Sellers" -> sorted.sort(Comparator.comparingDouble(
                    (Product p) -> AppContext.get().reviewService.getAverageRating(p.getSellerUsername())).reversed());
            default -> { /* Relevance - keep whatever order the search already returned */ }
        }
        return sorted;
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

    /** Real photo if the listing has one on disk, otherwise a category-icon placeholder tile - plus a wishlist heart. */
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

        Button heart = buildWishlistHeart(product);
        StackPane.setAlignment(heart, Pos.TOP_RIGHT);
        frame.getChildren().add(heart);

        return frame;
    }

    /** Small heart toggle button overlaid on the thumbnail - clicking it never opens the product (Button consumes the click). */
    private Button buildWishlistHeart(Product product) {
        String me = SessionManager.getCurrentUser().getUsername();
        boolean saved = AppContext.get().wishlistService.isWishlisted(me, product.getId());

        Button heart = new Button(saved ? "\u2665" : "\u2661");
        heart.getStyleClass().add(saved ? "wishlist-heart-button-active" : "wishlist-heart-button");

        heart.setOnAction(e -> {
            boolean nowSaved = AppContext.get().wishlistService.toggle(me, product.getId());
            heart.setText(nowSaved ? "\u2665" : "\u2661");
            heart.getStyleClass().removeAll("wishlist-heart-button", "wishlist-heart-button-active");
            heart.getStyleClass().add(nowSaved ? "wishlist-heart-button-active" : "wishlist-heart-button");
        });
        return heart;
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
