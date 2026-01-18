package com.hotel.management.system.controller;

import com.google.i18n.phonenumbers.Phonenumber;
import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Role;
import com.hotel.management.system.model.User;
import com.hotel.management.system.repository.UserRepository;
import com.hotel.management.system.security.CurrentUser;
import com.hotel.management.system.service.UserService;
import com.hotel.management.system.util.AlertUtil;
import com.hotel.management.system.util.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.Currency;
import java.util.UUID;

public class UsersController {

    @FXML private TextField usernameField, phoneField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleCombo;

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, String> phoneCol;
    @FXML private TableColumn<User, Void> actionsCol;

    private final ObservableList<User> users = FXCollections.observableArrayList();
    private UserService userService;
    private User curretnUser = CurrentUser.get();
    private User selectedUser;

    public void initialize() {
        userService = new UserService(new UserRepository(DB.INSTANCE));

        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));

        usernameCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername())
        );

        roleCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getRole().name())
        );

        phoneCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPhoneNumber() == null ? "N/A" : "+" + data.getValue().getPhoneNumber().getCountryCode()
                                + data.getValue().getPhoneNumber().getNationalNumber()
                )
        );

        usernameField.setDisable(false);
        roleCombo.setDisable(false);
        addActionsColumn();
        loadUsers();
        handleRowSelection();
    }

    private void loadUsers() {
        users.setAll(userService.getAllUsers());
        usersTable.setItems(users);
    }

    // ================= ADD / UPDATE =================

    @FXML
    public void onAddUser() {
        try {
            if (selectedUser == null) {
                Validator.required(usernameField, "Username");
                Validator.required(passwordField, "Password");
                Validator.required(roleCombo, "Role");
                Validator.required(phoneField, "Phone Number");
            } else {
                Validator.required(usernameField, "Username");
                Validator.required(roleCombo, "Role");
                Validator.required(phoneField, "Phone Number");
            }


            Phonenumber.PhoneNumber phoneNumber = null;

            String rawPhone = phoneField.getText().trim();
            if (!rawPhone.isEmpty()) {
                phoneNumber = new Phonenumber.PhoneNumber();
                phoneNumber.setCountryCode(961); // Lebanon
                phoneNumber.setNationalNumber(Long.parseLong(rawPhone));
            }

            if (selectedUser == null) {

                // ADD
                userService.createFullUser(
                        usernameField.getText(),
                        passwordField.getText(),
                        roleCombo.getValue(),
                        phoneNumber
                );
                AlertUtil.success("User added successfully");
            } else {
                // UPDATE
                selectedUser.setRole(roleCombo.getValue());
                if (!passwordField.getText().isBlank()) {
                    selectedUser.setPassword(passwordField.getText());
                }
                userService.createFullUser(
                        selectedUser.getUsername(),
                        selectedUser.getPassword(),
                        selectedUser.getRole(),
                        phoneNumber
                );
                AlertUtil.success("User updated successfully");
            }

            clearForm();
            loadUsers();
        } catch (Exception e){
            AlertUtil.error(e.getMessage());
        }
    }

    // ================= DELETE =================

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this user?",
                ButtonType.YES, ButtonType.NO);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                userService.deleteUser(user.getId());
                clearForm();
                loadUsers();
            }
        });
    }

    // ================= ACTIONS COLUMN =================

    private void addActionsColumn() {
        actionsCol.setCellFactory(col -> new TableCell<>() {

            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(5);

            {
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                box.setAlignment(Pos.CENTER);

                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                User rowUser = getTableView().getItems().get(getIndex());

                // Show delete ONLY if:
                // 1. current user is ADMIN
                // 2. row user is NOT the current user
                if (curretnUser.getRole() == Role.ADMIN &&
                        !rowUser.getId().equals(curretnUser.getId())) {

                    box.getChildren().setAll(deleteBtn);
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });
    }



    // ================= SELECTION =================

    private void handleRowSelection() {
        usersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, user) -> fillForm(user)
        );
    }

    private void fillForm(User user) {
        if (user == null) return;
        selectedUser = user;

        usernameField.setDisable(false);
        roleCombo.setDisable(false);

        usernameField.setText(user.getUsername());
        passwordField.setText("");
        roleCombo.setValue(user.getRole());

        phoneField.setText(
                user.getPhoneNumber() == null
                        ? "N/A"
                        : "" + user.getPhoneNumber().getNationalNumber()
        );

        if (selectedUser.getId().equals(CurrentUser.get().getId())) {
            usernameField.setDisable(true);
            roleCombo.setDisable(true);
        }
    }


    // ================= UTILS =================

    @FXML
    public void onCancel() {
        clearForm();
    }

    private void clearForm() {
        selectedUser = null;
        usernameField.clear();
        passwordField.clear();
        phoneField.clear();
        roleCombo.setValue(null);
        usernameField.setDisable(false);
        roleCombo.setDisable(false);
        usersTable.getSelectionModel().clearSelection();
    }

}
