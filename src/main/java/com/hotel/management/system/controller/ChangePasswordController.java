package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.User;
import com.hotel.management.system.repository.UserRepository;
import com.hotel.management.system.security.CurrentUser;
import com.hotel.management.system.service.UserService;
import com.hotel.management.system.util.AlertUtil;
import com.hotel.management.system.util.ValidationException;
import com.hotel.management.system.util.Validator;
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
        try {
            Validator.required(currentPasswordField, "Current Password");
            Validator.required(newPasswordField, "New Password");
            Validator.required(confirmPasswordField, "Confirm Password");

            Validator.passwordMatches(
                    user.getPassword(),
                    currentPasswordField.getText()
            );

            Validator.passwordDifferent(
                    user.getPassword(),
                    newPasswordField.getText()
            );

            Validator.passwordsMatch(
                    newPasswordField.getText(),
                    confirmPasswordField.getText()
            );

            userService.updatePassword(user.getId(), newPasswordField.getText());
            user.setPassword(newPasswordField.getText());
            AlertUtil.success("Password changed successfully");

            closeWindow();
        } catch (ValidationException e) {
            AlertUtil.error(e.getMessage());
        } catch (Exception e) {
            AlertUtil.error("Failed to update password.");
        }

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
