package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.model.Reservation;
import com.hotel.management.system.model.Room;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.GuestService;
import com.hotel.management.system.service.ReservationService;
import com.hotel.management.system.service.RoomService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.*;

public class CheckOutController {

    @FXML private ComboBox<Reservation> occupiedRoomsCombo;
    @FXML private Label checkinDateLabel;
    @FXML private Label nightsStayedLabel;
    @FXML private Label checkoutRoomNumber;

    @FXML private Label roomRateLabel;
    @FXML private Label nightsCountLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label finalAmountLabel;
    private Reservation reservation;
    private Boolean isCalculated = false;

    private final Map<UUID, String> guestNameCache = new HashMap<>();

    private String resolveGuestName(UUID guestId) {
        return guestNameCache.computeIfAbsent(guestId, id ->
                guestService.getGuestById(id)
                        .map(Guest::fullName)
                        .orElse("Unknown Guest")
        );
    }


    private GuestService guestService;
    private RoomService roomService;
    private ReservationService reservationService;

    public void initialize() {
        // Initialize services
        guestService = new GuestService(new GuestRepository(DB.INSTANCE));
        roomService = new RoomService(new RoomRepository(DB.INSTANCE), new ReservationRepository(DB.INSTANCE));
        reservationService = new ReservationService(
                new ReservationRepository(DB.INSTANCE),
                new RoomRepository(DB.INSTANCE),
                new GuestRepository(DB.INSTANCE),
                roomService
        );


        loadOccupiedRooms();

    }



    private void loadOccupiedRooms() {
        // Load only available rooms
        List<Reservation> occupiedReservations =
                reservationService.getAllOccupiedReservations();

        ObservableList<Reservation> items =
                FXCollections.observableArrayList(occupiedReservations);

        occupiedRoomsCombo.setItems(items);

        occupiedRoomsCombo.setConverter(new StringConverter<>() {

            @Override
            public String toString(Reservation reservation) {
                if (reservation == null){
                    return "";
                }

                return "Room "
                        + reservation.getRoom().getRoomNumber()
                        + "  |  "
                        + resolveGuestName(reservation.getGuest().id());
            }

            @Override
            public Reservation fromString(String string) {
                return items.stream()
                        .filter(r ->
                                ("Room " + r.getRoom().getRoomNumber()
                                        + " | "
                                        + r.getGuest().fullName())
                                        .equalsIgnoreCase(string)
                        )
                        .findFirst()
                        .orElse(null);
            }
        });

        // When a room is selected, update details labels
        occupiedRoomsCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldRoom, newReservation) -> {

            reservation = newReservation;

            if (newReservation != null) {
                checkinDateLabel.setText(newReservation.getCheckIn().toString());
                nightsStayedLabel.setText(String.valueOf(reservationService.getNumberOfNightsBetweenDates(newReservation.getCheckIn(), LocalDate.now())));
                checkoutRoomNumber.setText(String.valueOf(newReservation.getRoom().getRoomNumber()));
            } else {
                checkinDateLabel.setText("—");
                nightsStayedLabel.setText("—");
                checkoutRoomNumber.setText("—");
            }
        });
    }

    public void onCalculate() {
        if (!isInputValid()) {
            return;
        }

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

        isCalculated = true;

        // UI updates (formatted)
        roomRateLabel.setText(String.format("$%.2f", pricePerNight));
        nightsCountLabel.setText(String.valueOf(nightsStayed));
        subtotalLabel.setText(String.format("$%.2f", subtotal));
        taxLabel.setText(String.format("$%.2f", taxValue));
        finalAmountLabel.setText(String.format("$%.2f", totalPayment));
    }


    public void onCompleteCheckOut() {
        if(!isCalculated){
            showError("Please calculate the bill before");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/management/system/PaymentPopup.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Payments");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            PaymentController controller = loader.getController();
            controller.setPayment(reservation);

            stage.setOnHidden(e -> onClear());

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private boolean isInputValid() {
        if(occupiedRoomsCombo.getValue() == null){
            showError("Please choose a reservation.");
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

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onClear() {
        occupiedRoomsCombo.getSelectionModel().clearSelection();
        occupiedRoomsCombo.setValue(null);
        occupiedRoomsCombo.getEditor().clear();

        checkinDateLabel.setText("-");
        nightsStayedLabel.setText("-");
        checkoutRoomNumber.setText("-");

        roomRateLabel.setText("0.00");
        nightsCountLabel.setText("0");
        subtotalLabel.setText("0.00");
        taxLabel.setText("0.00");
        finalAmountLabel.setText("0.00");

        reservation = null;
        isCalculated = false;
    }


}
