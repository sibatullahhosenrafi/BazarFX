package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.Order;
import com.bazarfx.model.OrderStatus;
import com.bazarfx.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

/**
 * Week 6 - Relational Database with SQLite and JavaFX.
 *
 * Lists the signed-in buyer's own orders straight out of the SQLite
 * database (a live "SELECT ... WHERE buyer_username = ?" through
 * {@code OrderService.getByBuyer}) and lets them cancel a not-yet-delivered
 * order, which issues a real DELETE. A Timeline re-queries every couple of
 * seconds so status updates made in the background by OrderStatusSimulator
 * (Week 4 - concurrency) show up on their own.
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
        addCancelButtonColumn();

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

    private void addCancelButtonColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button cancelButton = new Button("Cancel");

            {
                cancelButton.getStyleClass().add("secondary-button");
                cancelButton.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    boolean cancelled = AppContext.get().orderService.cancelOrder(order.getId());
                    statusMessageLabel.setText(cancelled
                            ? "Order " + order.getId() + " cancelled."
                            : "Delivered orders can't be cancelled.");
                    loadOrders();
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
                cancelButton.setDisable(order.getStatus() == OrderStatus.DELIVERED);
                setGraphic(cancelButton);
            }
        });
    }

    @FXML
    private void onRefresh() {
        loadOrders();
    }
}
