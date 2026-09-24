package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.concurrency.NotificationService;
import com.bazarfx.util.SceneManager;
import com.bazarfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

/**
 * Shell screen: a left sidebar (Browse / Sell / Cart / My Orders / Dashboard)
 * plus a center pane whose content is swapped between screens, and a live
 * notification list fed by the background NotificationService. The sidebar
 * button matching whatever screen is currently open gets a highlighted
 * "active" style, same idea as the highlighted item in the reference design.
 */
public class MainController {

    @FXML private BorderPane centerPane;
    @FXML private Label welcomeLabel;
    @FXML private ListView<String> notificationList;
    @FXML private Button navBrowseButton;
    @FXML private Button navSellButton;
    @FXML private Button navCartButton;
    @FXML private Button navOrdersButton;
    @FXML private Button navDashboardButton;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Hi, " + SessionManager.getCurrentUser().getUsername());
        notificationList.setItems(NotificationService.getInstance().getNotifications());
        openBrowse();
    }

    private void setActiveNavButton(Button active) {
        for (Button b : new Button[]{navBrowseButton, navSellButton, navCartButton, navOrdersButton, navDashboardButton}) {
            if (b != null) b.getStyleClass().remove("side-nav-button-active");
        }
        if (active != null) active.getStyleClass().add("side-nav-button-active");
    }

    @FXML
    public void openBrowse() {
        setActiveNavButton(navBrowseButton);
        Parent[] rootHolder = new Parent[1];
        BrowseController controller = SceneManager.loadFragmentWithController("browse.fxml", rootHolder);
        controller.setMainController(this);
        centerPane.setCenter(rootHolder[0]);
    }

    @FXML
    private void openSell() {
        setActiveNavButton(navSellButton);
        Parent[] rootHolder = new Parent[1];
        SellController controller = SceneManager.loadFragmentWithController("sell.fxml", rootHolder);
        controller.setMainController(this);
        centerPane.setCenter(rootHolder[0]);
    }

    @FXML
    private void openCart() {
        setActiveNavButton(navCartButton);
        Parent[] rootHolder = new Parent[1];
        CartController controller = SceneManager.loadFragmentWithController("cart.fxml", rootHolder);
        controller.setMainController(this);
        centerPane.setCenter(rootHolder[0]);
    }

    @FXML
    private void openMyOrders() {
        setActiveNavButton(navOrdersButton);
        centerPane.setCenter(SceneManager.loadFragment("my_orders.fxml"));
    }

    @FXML
    private void openDashboard() {
        setActiveNavButton(navDashboardButton);
        centerPane.setCenter(SceneManager.loadFragment("dashboard.fxml"));
    }

    @FXML
    private void onLogout() {
        SessionManager.logout();
        AppContext.get().cart.clear();
        SceneManager.switchTo("login.fxml", "BazarFX - Log In");
    }

    /** Called from BrowseController when a listing is opened. */
    public void openProductDetail(String productId) {
        Parent[] rootHolder = new Parent[1];
        ProductDetailController controller =
                SceneManager.loadFragmentWithController("product_detail.fxml", rootHolder);
        controller.setMainController(this);
        controller.setProduct(productId);
        centerPane.setCenter(rootHolder[0]);
    }

    public void openCheckout() {
        centerPane.setCenter(SceneManager.loadFragment("checkout.fxml"));
    }
}
