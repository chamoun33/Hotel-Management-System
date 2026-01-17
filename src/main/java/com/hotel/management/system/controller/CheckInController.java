package com.hotel.management.system.controller;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hotel.management.system.MainController;
import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.model.Reservation;
import com.hotel.management.system.model.ReservationStatus;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.GuestService;
import com.hotel.management.system.service.ReservationService;
import com.hotel.management.system.service.RoomService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CheckInController {

    @FXML private ComboBox<Reservation> reservationComboBox;
    @FXML private Label guestNameLabel;
    @FXML private Label guestPhoneLabel;
    @FXML private Label roomNumberLabel;
    @FXML private Label checkInDateLabel;
    @FXML private Label checkOutDateLabel;


    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onCreateReservation() {
        if (mainController != null) {
            mainController.navigateTo("Booking.fxml");
        }
    }



    private ReservationService reservationService;
    private GuestService guestService;
    private RoomService roomService;

    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();
    private final Map<UUID, String> guestNameCache = new HashMap<>();
    private final Map<UUID, String> guestPhoneCache = new HashMap<>();


    private String resolveGuestName(UUID guestId) {
        return guestNameCache.computeIfAbsent(guestId, id ->
                guestService.getGuestById(id)
                        .map(Guest::fullName)
                        .orElse("Unknown Guest")
        );
    }
    private String resolveGuestPhoneNumber(UUID guestId) {
        return guestPhoneCache.computeIfAbsent(guestId, id ->
                guestService.getGuestById(id)
                        .map(guest -> guest.phoneNumber() != null
                                ? formatPhone(guest.phoneNumber())
                                : "N/A")
                        .orElse("Unknown Guest")
        );
    }

    private String formatPhone(Phonenumber.PhoneNumber phone) {
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        return util.format(phone, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }



    @FXML
    public void initialize() {

        GuestRepository guestRepository = new GuestRepository(DB.INSTANCE);
        RoomRepository roomRepository = new RoomRepository(DB.INSTANCE);
        ReservationRepository reservationRepository = new ReservationRepository(DB.INSTANCE);

        guestService = new GuestService(guestRepository);
        roomService = new RoomService(roomRepository, reservationRepository);
        reservationService = new ReservationService(
                reservationRepository,
                roomRepository,
                guestRepository,
                roomService
        );

        loadReservations();
    }

    private void loadReservations() {

        List<Reservation> allReservations = reservationService.getAllReservations().stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).toList();
        reservations.setAll(allReservations);

        FilteredList<Reservation> filteredReservations =
                new FilteredList<>(reservations, r -> true);

        reservationComboBox.setItems(filteredReservations);
        reservationComboBox.setEditable(true);

        // Display guest name in ComboBox
        reservationComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Reservation reservation) {
                if (reservation == null) return "";
                return resolveGuestName(reservation.getGuest().id());
            }

            @Override
            public Reservation fromString(String string) {
                return reservations.stream()
                        .filter(r -> resolveGuestName(r.getGuest().id())
                                .equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Search logic
        reservationComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            Reservation selected = reservationComboBox.getSelectionModel().getSelectedItem();
            if (selected == null ||
                    !resolveGuestName(selected.getGuest().id()).equals(newText)) {

                filteredReservations.setPredicate(r ->
                        resolveGuestName(r.getGuest().id())
                                .toLowerCase()
                                .contains(newText.toLowerCase())
                );

                reservationComboBox.show();
            }
        });

        // Selection listener → update UI
        reservationComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldRes, newRes) -> {
                    if (newRes == null) {
                        clearInfo();
                        return;
                    }

                    guestNameLabel.setText("Guest: " +
                            resolveGuestName(newRes.getGuest().id()));

                    guestPhoneLabel.setText("Phone: " +
                            (resolveGuestPhoneNumber(newRes.getGuest().id())));

                    roomNumberLabel.setText("Room: " + newRes.getRoom().getRoomNumber());

                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
                    checkInDateLabel.setText("Check-In Date: " +
                            newRes.getCheckIn().format(fmt));

                    checkOutDateLabel.setText("Check-Out Date: " +
                            newRes.getCheckOut().format(fmt));
                });
    }

    public void clearInfo() {
        guestNameLabel.setText("Guest: -");
        guestPhoneLabel.setText("Phone: -");
        roomNumberLabel.setText("Room: -");
        checkInDateLabel.setText("Check-In Date: -");
        checkOutDateLabel.setText("Check-Out Date: -");

        reservationComboBox.getSelectionModel().clearSelection();
        reservationComboBox.setValue(null);
        reservationComboBox.getEditor().clear();
    }


    public void onCheckIn() {
        if (!isInputValid()) {
            return;
        }

        reservationService.checkIn(reservationComboBox.getValue().getId());
        reservationService.setCheckIn(reservationComboBox.getValue().getId(), LocalDate.now());
        if (mainController != null) {
            mainController.loadDashboardData();
        }
        showSuccess("This room is checked In successfully");

        clearInfo();
        loadReservations();
    }

    private boolean isInputValid() {
        if(reservationComboBox.getValue() == null){
            showError("Please choose a reservation please.");
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

}
