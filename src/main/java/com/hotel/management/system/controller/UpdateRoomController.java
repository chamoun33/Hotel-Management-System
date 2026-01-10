package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.model.Room;
import com.hotel.management.system.model.RoomStatus;
import com.hotel.management.system.model.RoomType;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.RoomService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UpdateRoomController {

    @FXML private TextField roomNumberField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private TextField roomPriceField;
    @FXML private ComboBox<String> roomStatusCombo;
    @FXML private TextField capacityField;

    private RoomService roomService;


    @FXML
    public void initialize() {
        RoomRepository roomRepository = new RoomRepository(DB.INSTANCE);
        roomService = new RoomService(roomRepository);
    }

    private Room room;


    public void setRoom(Room room) {
        this.room = room;

        roomNumberField.setText(String.valueOf(room.getRoomNumber()));
        capacityField.setText(String.valueOf(room.getCapacity()));
        roomTypeCombo.setValue(room.getRoomType().name());
        roomPriceField.setText(String.valueOf(room.getPricePerNight()));
        roomStatusCombo.setValue(room.getStatus().name());

        roomNumberField.setDisable(true);
    }

    @FXML
    private void onUpdateRoom() {
        if (!isInputValid()) return;

        Room updatedRoom = new Room(
                room.getRoomNumber(), // keep same room number
                Integer.parseInt(capacityField.getText().trim()),
                RoomType.valueOf(roomTypeCombo.getValue().toUpperCase()),
                Double.parseDouble(roomPriceField.getText().trim()),
                RoomStatus.valueOf(roomStatusCombo.getValue().toUpperCase())
        );

        roomService.addRoom(updatedRoom); // your save method handles insert/update
        closeWindow();
    }

    private boolean isInputValid() {
        if (roomTypeCombo.getValue() == null || roomTypeCombo.getValue().isBlank()) {
            showError("Room type is required.");
            return false;
        }

        if (roomPriceField.getText() == null || roomPriceField.getText().isBlank()) {
            showError("Price per night is required.");
            return false;
        }

        if (roomStatusCombo.getValue() == null || roomStatusCombo.getValue().isBlank()) {
            showError("Room status is required.");
            return false;
        }

        if (capacityField.getText() == null || capacityField.getText().isBlank()) {
            showError("Capacity is required.");
            return false;
        }

        try {
            Integer.parseInt(capacityField.getText().trim());
            Double.parseDouble(roomPriceField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Capacity must be an integer and price must be a number.");
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
        Stage stage = (Stage) roomNumberField.getScene().getWindow();
        stage.close();
    }
}
