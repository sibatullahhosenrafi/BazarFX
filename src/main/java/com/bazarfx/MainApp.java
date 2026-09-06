package com.bazarfx;

import com.bazarfx.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/** JavaFX entry point. Boots straight into the login screen. */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.init(primaryStage);
        SceneManager.switchTo("login.fxml", "BazarFX - Log In");
    }

    @Override
    public void stop() {
        // Make sure background thread pools are cleaned up when the window closes.
        AppContext.get().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
