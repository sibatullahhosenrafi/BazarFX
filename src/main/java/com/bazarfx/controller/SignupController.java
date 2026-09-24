package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField locationField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    private void onSignup() {
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Passwords do not match.");
            return;
        }

        String error = AppContext.get().authService.signup(
                usernameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                locationField.getText().trim(),
                passwordField.getText());

        if (error != null) {
            errorLabel.setText(error);
        } else {
            SceneManager.switchTo("login.fxml", "BazarFX - Log In");
        }
    }

    @FXML
    private void onGoToLogin() {
        SceneManager.switchTo("login.fxml", "BazarFX - Log In");
    }
}
