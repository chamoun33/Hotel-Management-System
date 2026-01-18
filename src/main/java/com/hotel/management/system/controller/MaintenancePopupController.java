package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Room;
import com.hotel.management.system.model.RoomStatus;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.RoomService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class MaintenancePopupController {

    @FXML
    private TableView<Room> maintenanceTable;
    @FXML
    private TableColumn<Room, String> roomNumberCol;
    @FXML
    private TableColumn<Room, Void> actionCol; // For the button

    private RoomService roomService;
    private final ObservableList<Room> rooms = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        RoomRepository roomRepository = new RoomRepository(DB.INSTANCE);
        ReservationRepository reservationRepository = new ReservationRepository(DB.INSTANCE);

        roomService = new RoomService(roomRepository, reservationRepository);

        setupColumns();
        setupActionsColumn();
        loadRooms();
    }

    private void setupColumns() {
        roomNumberCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().getRoomNumber())
                )
        );

        // Center the text
        roomNumberCol.setStyle("-fx-alignment: CENTER;");
    }

    private void setupActionsColumn() {
        actionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Mark Available");

                    {
                        btn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-background-radius: 5;");
                        btn.setOnAction(event -> {
                            Room room = getTableView().getItems().get(getIndex());
                            room.setStatus(RoomStatus.AVAILABLE); // Update status
                            roomService.updateStatus(room.getRoomNumber(), RoomStatus.AVAILABLE);
                            loadRooms(); // Refresh table
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // Wrap button in HBox and center it
                            HBox hbox = new HBox(btn);
                            hbox.setAlignment(Pos.CENTER);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }


    private void loadRooms() {
        // Filter only rooms with status MAINTENANCE
        rooms.setAll(
                roomService.getAllRooms().stream()
                        .filter(r -> r.getStatus() == RoomStatus.MAINTENANCE)
                        .toList()
        );
        maintenanceTable.setItems(rooms);
    }

    @FXML
    private void onCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) maintenanceTable.getScene().getWindow();
        stage.close();
    }
}
