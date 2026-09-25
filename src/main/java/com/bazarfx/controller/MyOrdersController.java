package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.Order;
import com.bazarfx.model.OrderStatus;
import com.bazarfx.model.Product;
import com.bazarfx.model.Review;
import com.bazarfx.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Week 6 - Relational Database with SQLite and JavaFX.
 *
 * Lists the signed-in buyer's own orders straight out of the SQLite
 * database (a live "SELECT ... WHERE buyer_username = ?" through
 * {@code OrderService.getByBuyer}). The Action column is dual-purpose:
 *   - not yet delivered  -> "Cancel" button (real DELETE against orders)
 *   - delivered, no review yet -> "Review" button, opens a small rating +
 *     comment dialog and INSERTs a row into the reviews table, whose
 *     order_id is a FOREIGN KEY back to this exact order
 *   - delivered, already reviewed -> disabled "Reviewed" button
 * A Timeline re-queries every couple of seconds so status updates made in
 * the background by OrderStatusSimulator (Week 4 - concurrency) show up on
 * their own.
 */
public class MyOrdersController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> productColumn;
    @FXML private TableColumn<Order, Integer> quantityColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> dateColumn;
    @FXML private TableColumn<Order, Void> actionColumn;
    @FXML private Label statusMessageLabel;

    private Timeline refreshTimer;

    @FXML
    private void initialize() {
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productTitle"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().toString()));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        addActionColumn();

        loadOrders();

        refreshTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> loadOrders()));
        refreshTimer.setCycleCount(Timeline.INDEFINITE);
        refreshTimer.play();
    }

    private void loadOrders() {
        String username = SessionManager.getCurrentUser().getUsername();
        ObservableList<Order> data =
                FXCollections.observableArrayList(AppContext.get().orderService.getByBuyer(username));
        ordersTable.setItems(data);
    }

    /** One button that is either "Cancel" (active orders) or "Review" / "Reviewed" (delivered orders). */
    private void addActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button actionButton = new Button();

            {
                actionButton.getStyleClass().addAll("secondary-button", "table-action-button");
                actionButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    if (order.getStatus() == OrderStatus.DELIVERED) {
                        openReviewDialog(order);
                    } else {
                        boolean cancelled = AppContext.get().orderService.cancelOrder(order.getId());
                        statusMessageLabel.setText(cancelled
                                ? "Order " + order.getId() + " cancelled."
                                : "Delivered orders can't be cancelled.");
                        loadOrders();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                Order order = getTableView().getItems().get(getIndex());
                if (order.getStatus() == OrderStatus.DELIVERED) {
                    boolean alreadyReviewed = AppContext.get().reviewService.hasReview(order.getId());
                    actionButton.setText(alreadyReviewed ? "Reviewed" : "Review");
                    actionButton.setDisable(alreadyReviewed);
                } else {
                    actionButton.setText("Cancel");
                    actionButton.setDisable(false);
                }
                setGraphic(actionButton);
            }
        });
    }

    /**
     * Small modal dialog: pick a 1-5 star rating and write a comment, then
     * INSERT it into the reviews table via ReviewService/ReviewDao.
     * order.getId() becomes the review's order_id - the FOREIGN KEY value
     * that ties it back to this exact row in the orders table.
     *
     * A plain javafx.scene.control.Dialog is a separate Window with its own
     * Scene, so it never automatically picks up style.css or the app's
     * light/dark theme - it has to be wired in explicitly, which is what
     * the block right after `new Dialog<>()` below does.
     */
    private void openReviewDialog(Order order) {
        Product product = AppContext.get().productService.getById(order.getProductId());
        if (product == null) {
            statusMessageLabel.setText("Can't find the seller for this order - it may be old demo data.");
            return;
        }
        String sellerUsername = product.getSellerUsername();

        Dialog<Review> dialog = new Dialog<>();
        dialog.setTitle("Leave a Review");
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/com/bazarfx/style.css").toExternalForm());
        // Match whichever theme (light/dark) the main window is currently in -
        // "dark-mode" toggles the same looked-up color tokens this stylesheet uses.
        if (ordersTable.getScene() != null
                && ordersTable.getScene().getRoot().getStyleClass().contains("dark-mode")) {
            pane.getStyleClass().add("dark-mode");
        }

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
        Button submitButton = (Button) pane.lookupButton(submitButtonType);
        Button cancelButton = (Button) pane.lookupButton(ButtonType.CANCEL);
        submitButton.getStyleClass().addAll("primary-button");
        cancelButton.getStyleClass().addAll("secondary-button");

        // --- Header: icon + title + subtitle -----------------------------
        Label icon = new Label("\uD83D\uDECD\uFE0F"); // 🛍️
        icon.getStyleClass().add("review-dialog-icon");
        Label title = new Label("How was \"" + order.getProductTitle() + "\"?");
        title.setWrapText(true);
        title.getStyleClass().add("review-dialog-title");
        VBox titleBox = new VBox(2, title,
                labelWithClass("Your feedback helps other buyers trust " + sellerUsername + ".",
                        "review-dialog-subtitle"));
        HBox header = new HBox(12, icon, titleBox);
        header.setAlignment(Pos.CENTER_LEFT);

        // --- Star rating picker -------------------------------------------
        int[] selectedRating = {5};
        Label[] stars = new Label[5];
        HBox starRow = new HBox(4);
        starRow.getStyleClass().add("star-rating-row");
        Label ratingCaption = new Label();
        ratingCaption.getStyleClass().add("review-rating-caption");

        Runnable refreshStars = () -> {
            for (int i = 0; i < stars.length; i++) {
                boolean filled = i < selectedRating[0];
                stars[i].getStyleClass().removeAll("star-button-filled");
                if (filled) stars[i].getStyleClass().add("star-button-filled");
            }
            ratingCaption.setText(selectedRating[0] + " out of 5");
        };

        for (int i = 0; i < stars.length; i++) {
            int starValue = i + 1;
            Label star = new Label("\u2605"); // ★
            star.getStyleClass().add("star-button");
            star.setOnMouseClicked(e -> {
                selectedRating[0] = starValue;
                refreshStars.run();
            });
            stars[i] = star;
            starRow.getChildren().add(star);
        }
        refreshStars.run();

        VBox ratingBlock = new VBox(6,
                labelWithClass("Rating", "review-dialog-section-label"), starRow, ratingCaption);

        // --- Comment box ----------------------------------------------------
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("A short comment about the item or seller...");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);

        VBox commentBlock = new VBox(6,
                labelWithClass("Comment", "review-dialog-section-label"), commentArea);

        VBox content = new VBox(16, header, ratingBlock, commentBlock);
        content.getStyleClass().add("review-dialog-content");
        content.setPrefWidth(360);
        pane.setContent(content);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != submitButtonType) return null;
            String comment = commentArea.getText() == null ? "" : commentArea.getText().trim();
            return new Review(order.getId(), sellerUsername,
                    SessionManager.getCurrentUser().getUsername(),
                    selectedRating[0], comment);
        });

        dialog.showAndWait().ifPresent(review -> {
            AppContext.get().reviewService.addReview(review);
            statusMessageLabel.setText("Thanks - your review was saved.");
            loadOrders();
        });
    }

    private Label labelWithClass(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        return label;
    }

    @FXML
    private void onRefresh() {
        loadOrders();
    }
}
