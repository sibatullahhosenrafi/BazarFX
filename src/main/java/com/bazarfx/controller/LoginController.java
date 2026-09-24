package com.bazarfx.controller;

import com.bazarfx.AppContext;
import com.bazarfx.model.User;
import com.bazarfx.util.SceneManager;
import com.bazarfx.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        Optional<User> user = AppContext.get().authService.login(username, password);
        if (user.isPresent()) {
            SessionManager.login(user.get());
            SceneManager.switchTo("main.fxml", "BazarFX");
        } else {
            errorLabel.setText("Invalid username or password.");
        }
    }

    @FXML
    private void onGoToSignup() {
        SceneManager.switchTo("signup.fxml", "BazarFX - Sign Up");
    }
}
