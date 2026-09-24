package com.bazarfx.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Small helper that centralizes FXML loading and scene switching on the primary Stage. */
public class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    /** Loads an FXML file (path relative to /com/bazarfx/view/) and shows it full-window. */
    public static void switchTo(String fxmlFileName, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/bazarfx/view/" + fxmlFileName));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1000, 650);
            scene.getStylesheets().add(SceneManager.class.getResource("/com/bazarfx/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Could not load view: " + fxmlFileName, e);
        }
    }

    /** Loads an FXML file and returns its root node, for embedding inside another view. */
    public static Parent loadFragment(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/bazarfx/view/" + fxmlFileName));
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load fragment: " + fxmlFileName, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T loadFragmentWithController(String fxmlFileName, Parent[] rootOut) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/com/bazarfx/view/" + fxmlFileName));
            rootOut[0] = loader.load();
            return (T) loader.getController();
        } catch (IOException e) {
            throw new RuntimeException("Could not load fragment: " + fxmlFileName, e);
        }
    }
}
