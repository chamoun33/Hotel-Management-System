package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.repository.UserRepository;
import com.hotel.management.system.security.CurrentUser;
import com.hotel.management.system.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    private UserService userService;

    @FXML
    public void initialize() {
        userService = new UserService(
                new UserRepository(DB.INSTANCE)
        );
    }

    @FXML
    public void onLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }

        userService.authenticate(username, password)
                .ifPresentOrElse(
                        user -> {
                            CurrentUser.set(user);
                            openDashboard();
                        },
                        () -> showError("Invalid username or password.")
                );
    }

    private void openDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/management/system/MainDashboard.fxml")
            );
            Scene scene = new Scene(loader.load(), 1200, 840);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Unable to load dashboard.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Failed");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
