package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Room;
import com.hotel.management.system.model.RoomStatus;
import com.hotel.management.system.model.RoomType;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.RoomService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.UUID;

public class RoomController {

    @FXML private TextField roomNumberField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private TextField roomPriceField;
    @FXML private ComboBox<String> roomStatusCombo;
    @FXML private TextField capacityField;

    @FXML private TableView<Room> roomsTable;
    @FXML private TableColumn<Room, Integer> roomNumberColumn;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, Double> roomPriceColumn;
    @FXML private TableColumn<Room, String> roomStatusColumn;
    @FXML private TableColumn<Room, Integer> capacityColumn;
    @FXML private TableColumn<Room, Void> actionsColumn;


    private RoomService roomService;


    @FXML
    public void initialize() {
        RoomRepository roomRepository = new RoomRepository(DB.INSTANCE);
        roomService = new RoomService(roomRepository);

        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        roomPriceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        roomStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        capacityColumn.setCellValueFactory(new PropertyValueFactory<>("capacity"));

// Setup actions column
        setupActionsColumn();

    // Load all rooms
        loadRooms();
    }


    private void loadRooms() {
        roomsTable.getItems().clear();
        roomsTable.getItems().addAll(roomService.getAllRooms());
    }

    @FXML
    public void onAddRoom() {
        if (!isInputValid()) return;

        try {
            int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
            int capacity = Integer.parseInt(capacityField.getText().trim());
            double price = Double.parseDouble(roomPriceField.getText().trim());

            RoomType type = RoomType.valueOf(roomTypeCombo.getValue().toUpperCase());
            RoomStatus status = RoomStatus.valueOf(roomStatusCombo.getValue().toUpperCase());

            Room room = new Room(roomNumber, capacity, type, price, status);
            roomService.addRoom(room);

            loadRooms(); // refresh table

        } catch (NumberFormatException e) {
            showError("Room number, capacity and price must be numeric!");
        } catch (IllegalArgumentException e) {
            showError("Invalid Room Type or Status!");
        }
    }


    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox box = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                box.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    openEditRoomPopup(room);
                });

                deleteBtn.setOnAction(e -> {
                    Room room = getTableView().getItems().get(getIndex());
                    deleteRoom(room.getRoomNumber());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }


    private void deleteRoom(int roomNumber) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Room");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This room will be permanently deleted.");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                roomService.deleteRoom(roomNumber);
                loadRooms();
            }
        });
    }



    private boolean isInputValid() {

        if (roomNumberField.getText() == null || roomNumberField.getText().isBlank()) {
            showError("Room number is required.");
            return false;
        }

        if (roomTypeCombo.getValue() == null || roomTypeCombo.getValue().isBlank()) {
            showError("Room type is required.");
            return false;
        }

        if (roomPriceField.getText() == null || roomPriceField.getText().isBlank()) {
            showError("Room price is required.");
            return false;
        }

        if (roomStatusCombo.getValue() == null || roomStatusCombo.getValue().isBlank()) {
            showError("Room status is required.");
            return false;
        }

        if (capacityField.getText() == null || capacityField.getText().isBlank()) {
            showError("Room capacity is required.");
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


    private void openEditRoomPopup(Room room) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/management/system/UpdateRoom.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Update Room");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            UpdateRoomController controller = loader.getController();
            controller.setRoom(room);

            stage.setOnHidden(e -> loadRooms());

            stage.showAndWait();

            roomsTable.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
