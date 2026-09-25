package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.concurrency.NotificationService;
import com.bazarfx.util.SceneManager;
import com.bazarfx.util.SessionManager;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * Shell screen: a left sidebar (Browse / Sell / Cart / My Orders / Dashboard)
 * plus a center pane whose content is swapped between screens, and a live
 * notification list fed by the background NotificationService.
 *
 * Each sidebar button carries its own semantic CSS class (browse-btn, sell-btn,
 * cart-btn, orders-btn, dash-btn) so the active state gradient matches the
 * button's own color.
 *
 * The theme toggle button switches dark-mode on and off by adding / removing
 * the "dark-mode" CSS class from mainRoot. All looked-up color tokens are
 * redefined under .dark-mode in style.css, so child nodes inherit them
 * automatically without needing the class themselves.
 */
public class MainController {

    @FXML private BorderPane mainRoot;
    @FXML private BorderPane centerPane;
    @FXML private VBox       sidebarBox;
    @FXML private VBox       notifPanel;
    @FXML private Label      welcomeLabel;
    @FXML private ListView<String> notificationList;
    @FXML private Button     navBrowseButton;
    @FXML private Button     navSellButton;
    @FXML private Button     navCartButton;
    @FXML private Button     navOrdersButton;
    @FXML private Button     navDashboardButton;
    @FXML private Button     themeToggleButton;

    private boolean darkMode = false;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Hi, " + SessionManager.getCurrentUser().getUsername());
        notificationList.setItems(NotificationService.getInstance().getNotifications());
        bindResponsiveLayout();
        openBrowse();
    }

    // ── Layout Responsiveness ───────────────────────────────────────────────
    // Sidebar and the notifications panel are no longer fixed-pixel columns:
    // each is bound to a percentage of the window's own width (clamped so
    // they never get uselessly thin or absurdly wide), so resizing/maximizing
    // the stage visibly grows or shrinks them instead of leaving dead space
    // in the center pane.
    private void bindResponsiveLayout() {
        sidebarBox.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> clamp(232, mainRoot.getWidth() * 0.20, 300),
                mainRoot.widthProperty()));

        notifPanel.prefWidthProperty().bind(Bindings.createDoubleBinding(
                () -> clamp(200, mainRoot.getWidth() * 0.18, 280),
                mainRoot.widthProperty()));
    }

    private double clamp(double min, double value, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ── Theme toggle ──────────────────────────────────────────────────────────

    @FXML
    private void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) {
            mainRoot.getStyleClass().add("dark-mode");
            themeToggleButton.setText("\u263C  Light Mode");   // ☼
        } else {
            mainRoot.getStyleClass().remove("dark-mode");
            themeToggleButton.setText("\u263D  Dark Mode");    // ☽
        }
    }

    // ── Sidebar active-state management ──────────────────────────────────────

    private void setActiveNavButton(Button active) {
        for (Button b : new Button[]{
                navBrowseButton, navSellButton, navCartButton,
                navOrdersButton, navDashboardButton}) {
            if (b != null) b.getStyleClass().remove("side-nav-button-active");
        }
        if (active != null) active.getStyleClass().add("side-nav-button-active");
    }

    // ── Navigation actions ────────────────────────────────────────────────────

    @FXML
    public void openBrowse() {
        setActiveNavButton(navBrowseButton);
        Parent[] rootHolder = new Parent[1];
        BrowseController controller =
                SceneManager.loadFragmentWithController("browse.fxml", rootHolder);
        controller.setMainController(this);
        centerPane.setCenter(rootHolder[0]);
    }

    @FXML
    private void openSell() {
        setActiveNavButton(navSellButton);
        Parent[] rootHolder = new Parent[1];
        SellController controller =
                SceneManager.loadFragmentWithController("sell.fxml", rootHolder);
        controller.setMainController(this);
        centerPane.setCenter(rootHolder[0]);
    }

    @FXML
    private void openCart() {
        setActiveNavButton(navCartButton);
        Parent[] rootHolder = new Parent[1];
        CartController controller =
                SceneManager.loadFragmentWithController("cart.fxml", rootHolder);
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

    // ── Called from child controllers ─────────────────────────────────────────

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
