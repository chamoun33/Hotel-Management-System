package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.model.Room;
import com.hotel.management.system.model.RoomStatus;
import com.hotel.management.system.model.RoomType;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.RoomService;
import com.hotel.management.system.util.AlertUtil;
import com.hotel.management.system.util.ValidationException;
import com.hotel.management.system.util.Validator;
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
        if(room.getStatus().name().equals(RoomStatus.OCCUPIED.name())){
            roomStatusCombo.setDisable(true);
        }

        roomNumberField.setDisable(true);
    }

    @FXML
    private void onUpdateRoom() {

        try {

            Validator.required(roomTypeCombo, "Room type");
            Validator.required(roomPriceField, "Room price");
            Validator.required(roomStatusCombo, "Room status");
            Validator.required(capacityField, "Capacity");

            int capacity = Validator.integer(capacityField, "Capacity");
            double price = Validator.decimal(roomPriceField, "Room price");
            RoomType type = RoomType.valueOf(roomTypeCombo.getValue().toUpperCase());
            RoomStatus status = RoomStatus.valueOf(roomStatusCombo.getValue().toUpperCase());

            Room updatedRoom = new Room(
                    room.getRoomNumber(), // keep same room number
                    capacity,
                    type,
                    price,
                    status
            );

            roomService.addRoom(updatedRoom); // your save method handles insert/update
            closeWindow();

        } catch (ValidationException e) {
            AlertUtil.error(e.getMessage());
        } catch (IllegalArgumentException e) {
            AlertUtil.error("Invalid room type or status.");
        } catch (Exception e) {
            AlertUtil.error("An unexpected error occurred.");
        }
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
