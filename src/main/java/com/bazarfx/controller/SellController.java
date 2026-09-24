package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.Product;
import com.bazarfx.concurrency.NotificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SellController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryField;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> conditionField;
    @FXML private TextField locationField;
    @FXML private ListView<String> chosenPhotosList;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator progressIndicator;

    private MainController mainController;
    private final List<File> chosenFiles = new ArrayList<>();

    @FXML
    private void initialize() {
        categoryField.setItems(FXCollections.observableArrayList(
                "Electronics", "Vehicles", "Furniture", "Fashion", "Books", "Other"));
        conditionField.setItems(FXCollections.observableArrayList("New", "Used"));
        progressIndicator.setVisible(false);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onChoosePhotos() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose up to 5 photos");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        List<File> files = chooser.showOpenMultipleDialog(titleField.getScene().getWindow());
        if (files != null) {
            chosenFiles.clear();
            chosenFiles.addAll(files.size() > 5 ? files.subList(0, 5) : files);
            ObservableList<String> names = FXCollections.observableArrayList();
            for (File f : chosenFiles) names.add(f.getName());
            chosenPhotosList.setItems(names);
        }
    }

    @FXML
    private void onPublish() {
        String title = titleField.getText().trim();
        String priceText = priceField.getText().trim();

        if (title.isEmpty() || priceText.isEmpty() || categoryField.getValue() == null
                || conditionField.getValue() == null) {
            statusLabel.setText("Please fill in title, price, category and condition.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            statusLabel.setText("Price must be a number.");
            return;
        }

        Product product = new Product(
                com.bazarfx.util.SessionManager.getCurrentUser().getUsername(),
                title,
                descriptionField.getText().trim(),
                categoryField.getValue(),
                price,
                conditionField.getValue(),
                locationField.getText().trim());

        AppContext.get().productService.addProduct(product);

        if (chosenFiles.isEmpty()) {
            finishPublish(product.getTitle());
        } else {
            progressIndicator.setVisible(true);
            statusLabel.setText("Uploading photos in the background...");
            AppContext.get().imageProcessor.processImages(
                    product.getId(),
                    chosenFiles,
                    product::addImagePath,
                    () -> {
                        AppContext.get().productService.persist();
                        progressIndicator.setVisible(false);
                        finishPublish(product.getTitle());
                    });
        }
    }

    private void finishPublish(String title) {
        statusLabel.setText("\"" + title + "\" published successfully!");
        NotificationService.getInstance().push("Your listing \"" + title + "\" is now live.");
        clearForm();
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        priceField.clear();
        locationField.clear();
        categoryField.setValue(null);
        conditionField.setValue(null);
        chosenFiles.clear();
        chosenPhotosList.getItems().clear();
    }
}
