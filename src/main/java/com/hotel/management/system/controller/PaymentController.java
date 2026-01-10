package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.*;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.PaymentRepository;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.GuestService;
import com.hotel.management.system.service.PaymentService;
import com.hotel.management.system.service.ReservationService;
import com.hotel.management.system.service.RoomService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

public class PaymentController {

    @FXML private Label roomNumberLabel;
    @FXML private Label nighStayedLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalAmountLabel;

    @FXML private TextField amountReceivedField;
    @FXML private ComboBox<PaymentMethod> paymentMethodComboBox;

    private Reservation reservation;

    private RoomService roomService;
    private ReservationService reservationService;
    private PaymentService paymentService;

    public void initialize() {
        // Initialize services
        roomService = new RoomService(new RoomRepository(DB.INSTANCE), new ReservationRepository(DB.INSTANCE));
        reservationService = new ReservationService(
                new ReservationRepository(DB.INSTANCE),
                new RoomRepository(DB.INSTANCE),
                new GuestRepository(DB.INSTANCE),
                roomService
        );
        paymentService = new PaymentService(
                new PaymentRepository(
                        DB.INSTANCE,
                        new ReservationRepository(DB.INSTANCE)
                )
        );

        paymentMethodComboBox.setItems(
                FXCollections.observableArrayList(PaymentMethod.values())
        );

    }

    public void setPayment(Reservation reservation) {
        this.reservation = reservation;

        Room room = roomService
                .getRoom(reservation.getRoom().getRoomNumber())
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        double pricePerNight = room.getPricePerNight();

        long nightsStayed = reservationService
                .getNumberOfNightsBetweenDates(reservation.getCheckIn(), LocalDate.now());

        // Minimum 1 night
        if (nightsStayed <= 0) {
            nightsStayed = 1;
        }

        double subtotal = pricePerNight * nightsStayed;
        double taxValue = subtotal * 0.11;
        double totalPayment = subtotal + taxValue;

        roomNumberLabel.setText(String.format("$%.2f", pricePerNight));
        nighStayedLabel.setText(String.valueOf(nightsStayed));
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", taxValue));
        totalAmountLabel.setText(String.format("$%.2f", totalPayment));
    }

    @FXML
    public void onSubmitPayment() {
        if (!isInputValid()) {
            return;
        }

        try {
            double totalPayment = Double.parseDouble(totalAmountLabel.getText().replace("$", ""));
            double amountReceived = Double.parseDouble(amountReceivedField.getText().trim());
            PaymentMethod method = paymentMethodComboBox.getValue();

            if (amountReceived < totalPayment) {
                showError("Received amount cannot be less than total payment.");
                return;
            }

            // Make payment
            paymentService.makePayment(reservation, totalPayment, method);

            // Optional: show success alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Payment Successful");
            alert.setHeaderText(null);
            alert.setContentText(String.format("Payment of $%.2f received successfully via %s.", totalPayment, method));
            alert.showAndWait();

            // Close the window
            closeWindow();

        } catch (NumberFormatException e) {
            showError("Invalid amount entered.");
        }
    }


    private boolean isInputValid() {
        if(amountReceivedField.getText() == null || amountReceivedField.getText().isBlank()){
            showError("Please enter the amount received.");
            return false;
        }
        else if(paymentMethodComboBox.getValue() == null){
            showError("Please choose a payment method.");
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
        Stage stage = (Stage) roomNumberLabel.getScene().getWindow();
        stage.close();
    }

}
