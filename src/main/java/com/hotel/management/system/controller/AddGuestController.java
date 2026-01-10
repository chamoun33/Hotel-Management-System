package com.hotel.management.system.controller;

import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.service.GuestService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;



import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class AddGuestController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;

    private GuestService guestService;


    @FXML
    public void initialize() {
        GuestRepository guestRepository = new GuestRepository(DB.INSTANCE);
        guestService = new GuestService(guestRepository);
    }


    @FXML
    private void onAddGuest() {

        if (!isInputValid()) {
            return;
        }

        try {
            Phonenumber.PhoneNumber phoneNumber = null;

            String rawPhone = phoneField.getText().trim();
            if (!rawPhone.isEmpty()) {
                phoneNumber = new Phonenumber.PhoneNumber();
                phoneNumber.setCountryCode(961); // Lebanon
                phoneNumber.setNationalNumber(Long.parseLong(rawPhone));
            }


            Guest guest = new Guest(
                    UUID.randomUUID(),
                    firstNameField.getText().trim() + " " + lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneNumber
            );

            guestService.addGuest(guest);
        }catch(NumberFormatException e){
            showError("Phone number must be numeric!");
        }catch(Exception e){
            showError("An error has occurred");
        }finally {
            closeWindow();
        }
    }



    private boolean isInputValid() {

        if (firstNameField.getText() == null || firstNameField.getText().isBlank()) {
            showError("First name is required.");
            return false;
        }

        if (lastNameField.getText() == null || lastNameField.getText().isBlank()) {
            showError("Last name is required.");
            return false;
        }

        if (phoneField.getText() == null || phoneField.getText().isBlank()) {
            showError("Phone Number is required.");
            return false;
        }

        if (emailField.getText() == null || emailField.getText().isBlank()) {
            showError("Email is required.");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}
