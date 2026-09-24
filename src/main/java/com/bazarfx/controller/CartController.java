package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.CartItem;
import com.bazarfx.model.Product;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class CartController {

    @FXML private ListView<String> cartListView;
    @FXML private Label totalLabel;

    private MainController mainController;

    @FXML
    private void initialize() {
        refresh();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void refresh() {
        java.util.List<String> lines = new java.util.ArrayList<>();
        double total = 0;
        for (CartItem item : AppContext.get().cart) {
            Product p = AppContext.get().productService.getById(item.getProductId());
            if (p == null) continue;
            double lineTotal = p.getPrice() * item.getQuantity();
            total += lineTotal;
            lines.add(p.getTitle() + "   x" + item.getQuantity() + "   -   BDT " + lineTotal);
        }
        cartListView.setItems(FXCollections.observableArrayList(lines));
        totalLabel.setText("Total: BDT " + total);
    }

    @FXML
    private void onRemoveSelected() {
        int index = cartListView.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < AppContext.get().cart.size()) {
            AppContext.get().cart.remove(index);
            refresh();
        }
    }

    @FXML
    private void onCheckout() {
        if (!AppContext.get().cart.isEmpty()) {
            mainController.openCheckout();
        }
    }
}
