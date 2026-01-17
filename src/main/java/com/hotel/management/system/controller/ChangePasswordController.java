package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.User;
import com.hotel.management.system.repository.UserRepository;
import com.hotel.management.system.security.CurrentUser;
import com.hotel.management.system.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Objects;

public class ChangePasswordController {
    @FXML private TextField currentPasswordField, newPasswordField, confirmPasswordField;

    private UserService userService;

    public void initialize() {
        UserRepository userRepository = new UserRepository(DB.INSTANCE);

        userService = new UserService(userRepository);

    }

    private User user = CurrentUser.get();


    @FXML public void onUpdatePassword() {
        if(!isInputValid()){
            return;
        }

        userService.updatePassword(user.getId(), newPasswordField.getText());
        user.setPassword(newPasswordField.getText());
        showSuccess("Password changed successfully");

        closeWindow();


    }

    private boolean isInputValid() {
        if (currentPasswordField.getText() == null || currentPasswordField.getText().isBlank()) {
            showError("Current password is required.");
            return false;
        }

        if (newPasswordField.getText() == null || newPasswordField.getText().isBlank()) {
            showError("New Password is required.");
            return false;
        }

        if (confirmPasswordField.getText() == null || confirmPasswordField.getText().isBlank()) {
            showError("Confirm Password is required.");
            return false;
        }

        if (!Objects.equals(currentPasswordField.getText(), user.getPassword())){
            showError("Current password is incorrect");
            return false;
        }

        if (!Objects.equals(newPasswordField.getText(), confirmPasswordField.getText())){
            showError("New password doesn't match!!");
            return false;
        }

        if (Objects.equals(newPasswordField.getText(), currentPasswordField.getText())){
            showError("New password match the current password");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) currentPasswordField.getScene().getWindow();
        stage.close();
    }
}
