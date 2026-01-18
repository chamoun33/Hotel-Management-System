package com.hotel.management.system.controller;


import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.service.GuestService;
import com.hotel.management.system.util.AlertUtil;
import com.hotel.management.system.util.ValidationException;
import com.hotel.management.system.util.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class UpdateGuestController {

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

    private Guest guest;


    public void setGuest(Guest guest) {
        this.guest = guest;

        String[] nameParts = guest.fullName().split(" ", 2);

        firstNameField.setText(nameParts[0]);
        lastNameField.setText(nameParts.length > 1 ? nameParts[1] : "");
        emailField.setText(guest.email());

        if (guest.phoneNumber() != null) {
            phoneField.setText(
                    String.valueOf(guest.phoneNumber().getNationalNumber())
            );
        }
    }

    @FXML
    private void onUpdateGuest() {

        try {
            Validator.required(firstNameField, "First name");
            Validator.required(lastNameField, "Last name");
            Validator.required(phoneField, "Phone number");
            Validator.required(emailField, "Email");

            Validator.numeric(phoneField, "Phone number");
            Validator.email(emailField);

            Phonenumber.PhoneNumber phone = new Phonenumber.PhoneNumber();
            phone.setCountryCode(961);
            phone.setNationalNumber(Long.parseLong(phoneField.getText().trim()));

            Guest updatedGuest = new Guest(
                    guest.id(),
                    firstNameField.getText().trim() + " " + lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phone
            );

            guestService.updateGuest(updatedGuest);
            closeWindow();

        } catch (ValidationException e) {
            AlertUtil.error(e.getMessage());
        } catch (Exception e) {
            AlertUtil.error("An unexpected error occurred.");
        }
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
