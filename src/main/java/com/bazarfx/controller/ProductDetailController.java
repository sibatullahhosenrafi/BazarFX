package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.CartItem;
import com.bazarfx.model.Product;
import com.bazarfx.concurrency.NotificationService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.File;

public class ProductDetailController {

    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label sellerLabel;
    @FXML private Label conditionLabel;
    @FXML private Label locationLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label ratingLabel;
    @FXML private ImageView photoView;
    @FXML private HBox thumbnailBox;

    private MainController mainController;
    private Product product;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setProduct(String productId) {
        this.product = AppContext.get().productService.getById(productId);
        AppContext.get().productService.incrementViews(productId);

        titleLabel.setText(product.getTitle());
        priceLabel.setText("BDT " + product.getPrice());
        sellerLabel.setText("Seller: " + product.getSellerUsername());
        conditionLabel.setText("Condition: " + product.getCondition());
        locationLabel.setText("Location: " + product.getLocation());
        descriptionLabel.setText(product.getDescription());

        double avg = AppContext.get().reviewService.getAverageRating(product.getSellerUsername());
        ratingLabel.setText(avg == 0 ? "No ratings yet" : String.format("Seller rating: %.1f / 5", avg));

        buildThumbnails();

        if (!product.getImagePaths().isEmpty()) {
            showMainPhoto(product.getImagePaths().get(0));
        } else {
            photoView.setImage(null);
        }
    }

    /** Builds one small clickable ImageView per uploaded photo below the main image. */
    private void buildThumbnails() {
        thumbnailBox.getChildren().clear();

        for (String path : product.getImagePaths()) {
            File file = new File(path);
            if (!file.exists()) continue;

            ImageView thumb = new ImageView(new Image(file.toURI().toString(), 64, 64, true, true));
            thumb.setFitWidth(64);
            thumb.setFitHeight(64);
            thumb.setPreserveRatio(true);
            thumb.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 3, 0.1, 0, 1);");
            HBox.setMargin(thumb, new Insets(2));

            thumb.setOnMouseClicked(e -> showMainPhoto(path));
            thumbnailBox.getChildren().add(thumb);
        }
    }

    private void showMainPhoto(String path) {
        File imageFile = new File(path);
        if (imageFile.exists()) {
            photoView.setImage(new Image(imageFile.toURI().toString(), 260, 200, true, true));
        }
    }

    @FXML
    private void onAddToCart() {
        AppContext.get().cart.add(new CartItem(product.getId(), 1));
        NotificationService.getInstance().push("Added \"" + product.getTitle() + "\" to your cart.");
    }

    @FXML
    private void onAddToWishlist() {
        if (!AppContext.get().wishlistProductIds.contains(product.getId())) {
            AppContext.get().wishlistProductIds.add(product.getId());
            NotificationService.getInstance().push("Saved \"" + product.getTitle() + "\" to your wishlist.");
        }
    }

    @FXML
    private void onBuyNow() {
        AppContext.get().cart.add(new CartItem(product.getId(), 1));
        mainController.openCheckout();
    }

    @FXML
    private void onBackToBrowse() {
        mainController.openBrowse();
    }
}