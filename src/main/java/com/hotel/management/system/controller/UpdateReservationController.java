package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.*;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.GuestService;
import com.hotel.management.system.service.ReservationService;
import com.hotel.management.system.service.RoomService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateReservationController {

    @FXML private Label guestNameLabel;
    @FXML private Label roomNumberLabel;
    @FXML private ComboBox<ReservationStatus> statusComboBox;
//    @FXML private DatePicker checkOutDatePicker;

    private ReservationService reservationService;
    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private GuestService guestService;
    private RoomService roomService;
    private final Map<UUID, String> guestNameCache = new HashMap<>();

    private String resolveGuestName(UUID guestId) {
        return guestNameCache.computeIfAbsent(guestId, id ->
                guestService.getGuestById(id)
                        .map(Guest::fullName)
                        .orElse("Unknown Guest")
        );
    }


    @FXML
    public void initialize() {

        GuestRepository guestRepository = new GuestRepository(DB.INSTANCE);
        RoomRepository roomRepository = new RoomRepository(DB.INSTANCE);
        ReservationRepository reservationRepository = new ReservationRepository(DB.INSTANCE);

        guestService = new GuestService(guestRepository);

        roomService = new RoomService(
                roomRepository,
                reservationRepository
        );

        reservationService = new ReservationService(
                reservationRepository,
                roomRepository,
                guestRepository,
                roomService
        );


        statusComboBox.setItems(
                FXCollections.observableArrayList(ReservationStatus.values())
        );


    }

    private Reservation reservation;

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;

        guestNameLabel.setText(resolveGuestName(reservation.getGuest().id()));
        roomNumberLabel.setText(
                String.valueOf(reservation.getRoom().getRoomNumber())
        );


        statusComboBox.setValue(reservation.getStatus());
//        checkOutDatePicker.setValue(reservation.getCheckOut());

        if(reservation.getStatus() == ReservationStatus.CHECKED_OUT){
            statusComboBox.setDisable(true);
        }
    }


    @FXML
    private void onUpdateReservation() {

        reservationService.updateStatus(reservation.getId(), statusComboBox.getValue()); // your save method handles insert/update
        closeWindow();
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
