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
import com.hotel.management.system.util.AlertUtil;
import com.hotel.management.system.util.ValidationException;
import com.hotel.management.system.util.Validator;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class BookingController {


    @FXML private TabPane tabPane;
    @FXML private Tab reservationsTab;

    @FXML private ReservationTableController reservationTableController;



    @FXML private ComboBox<Guest> guestComboBox;
    @FXML private ComboBox<Room> roomComboBox;
    @FXML private VBox guestInfoBox;
    @FXML private Label guestNameLabel;
    @FXML private Label guestPhoneLabel;
    @FXML private Label guestEmailLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label roomCapacityLabel;
    @FXML private Label roomPriceLabel;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;

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

        loadGuests();

        loadRooms();

        reservationsTab.setOnSelectionChanged(event -> {
            if (reservationsTab.isSelected() && reservationTableController != null) {
                reservationTableController.reload();
            }
        });
    }



    private void loadGuests() {
        List<Guest> guests = guestService.getAllGuests();
        FilteredList<Guest> filteredGuests = new FilteredList<>(FXCollections.observableArrayList(guests), g -> true);
        guestComboBox.setItems(filteredGuests);

        // Show only full name
        guestComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Guest guest) {
                return guest == null ? "" : guest.fullName();
            }

            @Override
            public Guest fromString(String string) {
                // Return the first match from the original guest list
                return guests.stream()
                        .filter(g -> g.fullName().equalsIgnoreCase(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        guestComboBox.setEditable(true);

        // Filter based on user typing
        guestComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            Guest selected = guestComboBox.getSelectionModel().getSelectedItem();
            // Only filter if user is typing, not selecting an item
            if (selected == null || !selected.fullName().equals(newText)) {
                filteredGuests.setPredicate(g -> g.fullName().toLowerCase().contains(newText.toLowerCase()));
                guestComboBox.show();
            }
        });

        // Show guest info when selected
        guestComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldGuest, newGuest) -> {
            if (newGuest != null) {
                guestNameLabel.setText("Name: " + newGuest.fullName());
                guestPhoneLabel.setText("Phone: " +
                        (newGuest.phoneNumber() != null
                                ? "+" + newGuest.phoneNumber().getCountryCode() + newGuest.phoneNumber().getNationalNumber()
                                : "N/A"));
                guestEmailLabel.setText("Email: " + newGuest.email());

                guestInfoBox.setManaged(true);
                guestInfoBox.setVisible(true);
            } else {
                guestInfoBox.setManaged(false);
                guestInfoBox.setVisible(false);
            }
        });
    }



    private void loadRooms() {
        // Load only available rooms
        List<Room> availableRooms = roomService.getAllRooms().stream()
                .filter(Room::isAvailable)
                .toList();

        roomComboBox.getItems().setAll(availableRooms);

        // Display only room numbers
        roomComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Room room) {
                return room == null ? "" : String.valueOf(room.getRoomNumber());
            }

            @Override
            public Room fromString(String string) {
                return availableRooms.stream()
                        .filter(r -> r.getRoomNumber() == Integer.parseInt(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // When a room is selected, update details labels
        roomComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldRoom, newRoom) -> {
            if (newRoom != null) {
                roomTypeLabel.setText(newRoom.getRoomType().name());
                roomPriceLabel.setText(String.valueOf(newRoom.getPricePerNight()));
                roomCapacityLabel.setText(String.valueOf(newRoom.getCapacity()));
            } else {
                roomTypeLabel.setText("—");
                roomPriceLabel.setText("—");
                roomCapacityLabel.setText("—");
            }
        });
    }



    @FXML
    public void openAddGuestPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/management/system/AddGuest.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Add Guest");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.setOnHidden(e -> loadGuests());

            stage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void confirmBooking() {

        try {
            Validator.required(guestComboBox, "Guest");
            Validator.required(roomComboBox, "Room");
            Validator.required(checkInDatePicker, "Check-in date");
            Validator.required(checkOutDatePicker, "Check-out date");

            Validator.dateNotInPast(checkInDatePicker, "Check-in date");
            Validator.dateNotInPast(checkOutDatePicker, "Check-out date");
            Validator.dateAfter(
                    checkInDatePicker,
                    checkOutDatePicker,
                    "Check-in date",
                    "Check-out date"
            );

            Guest guest = guestComboBox.getValue();
            Room room = roomComboBox.getValue();

            reservationService.createReservation(
                    guest.id(),
                    room.getRoomNumber(),
                    checkInDatePicker.getValue(),
                    checkOutDatePicker.getValue()
            );

            AlertUtil.info("Reservation confirmed for room " + room.getRoomNumber());
            clearForm();

        } catch (Exception e) {
            AlertUtil.error(e.getMessage());
        }
    }



    private void clearForm() {
        guestComboBox.getSelectionModel().clearSelection();
        roomComboBox.getSelectionModel().clearSelection();
        checkInDatePicker.setValue(null);
        checkOutDatePicker.setValue(null);
    }

    @FXML
    public void cancel() {
        clearForm();
    }

}
