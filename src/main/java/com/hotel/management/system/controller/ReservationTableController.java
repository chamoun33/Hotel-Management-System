package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.model.Reservation;
import com.hotel.management.system.model.ReservationStatus;
import com.hotel.management.system.model.Room;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.GuestService;
import com.hotel.management.system.service.ReservationService;
import com.hotel.management.system.service.RoomService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReservationTableController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> guestColumn;
    @FXML private TableColumn<Reservation, String> roomColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> checkOutColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, Void> actionsColumn;

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

        setupColumns();
        setupActionsColumn();
        loadReservations();
    }


    private void setupColumns() {

        guestColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        resolveGuestName(data.getValue().getGuest().id())
                )
        );

        roomColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().getRoom().getRoomNumber())
                )
        );

        checkInColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCheckIn().toString()
                )
        );

        checkOutColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCheckOut().toString()
                )
        );

        statusColumn.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getStatus().name()
                )
        );
    }

    private void loadReservations() {
        reservations.setAll(reservationService.getAllReservations());
        reservationTable.setItems(reservations);
    }

    public void reload() {
        loadReservations();
    }

    // ================= ACTION COLUMN =================

    private void setupActionsColumn() {

        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color:#2196f3;-fx-text-fill:white;");
                deleteBtn.setStyle("-fx-background-color:#f44336;-fx-text-fill:white;");

                editBtn.setOnAction(e -> {
                    Reservation reservation = getTableView()
                            .getItems().get(getIndex());
                    openEditPopup(reservation);
                });

                deleteBtn.setOnAction(e -> {
                    Reservation reservation = getTableView()
                            .getItems().get(getIndex());
                    deleteReservation(reservation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, editBtn, deleteBtn);
                    box.setStyle("-fx-alignment:center;");
                    setGraphic(box);
                }
            }
        });
    }

    private void deleteReservation(Reservation reservation) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Reservation");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This reservation will be permanently deleted.");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                reservationService.deleteReservation(reservation.getId());
                loadReservations();
            }
        });
    }

    private void openEditPopup(Reservation reservation) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/management/system/UpdateReservation.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Edit Reservation");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            UpdateReservationController controller = loader.getController();
            controller.setReservation(reservation);

            stage.setOnHidden(e -> loadReservations());

            stage.showAndWait();

            reservationTable.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
