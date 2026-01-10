package com.hotel.management.system.controller;

import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.model.Reservation;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.IGuestRepository;
import com.hotel.management.system.service.GuestService;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.UUID;

public class GuestsController {
    private ObservableList<Guest> guestData; // original data
    private FilteredList<Guest> filteredData;


    private final GuestService guestService;

    public GuestsController() {
        IGuestRepository guestRepository =
                new GuestRepository(DB.INSTANCE);

        this.guestService =
                new GuestService(guestRepository);
    }

    @FXML private TableView<Guest> guestsTable;

    @FXML private TableColumn<Guest, String> nameColumn;

    @FXML private TableColumn<Guest, String> phoneColumn;

    @FXML private TableColumn<Guest, String> emailColumn;

    @FXML private TableColumn<Guest, Void> actionsColumn;

    @FXML private TextField searchField;

    @FXML private Label totalGuestsLabel;

    @FXML
    private void onClearSearch() {
        searchField.clear(); // triggers listener and resets table
    }


    @FXML
    public void initialize() {

        nameColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().fullName())
        );

        emailColumn.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().email())
        );


        phoneColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().phoneNumber() == null
                                ? ""
                                : "+" + cellData.getValue().phoneNumber().getCountryCode()
                                + cellData.getValue().phoneNumber().getNationalNumber()
                )
        );

        setupActionsColumn();
        loadGuests();


        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(guest -> {
                if (newValue == null || newValue.isBlank()) {
                    return true; // show all if search is empty
                }

                String lowerCaseFilter = newValue.toLowerCase();

                // Search by full name (you can add more fields later)
                if (guest.fullName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Optional: search by email or phone
                if (guest.email() != null && guest.email().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (guest.phoneNumber() != null) {
                    String phone = "+" + guest.phoneNumber().getCountryCode() + guest.phoneNumber().getNationalNumber();
                    if (phone.contains(lowerCaseFilter)) {
                        return true;
                    }
                }

                return false; // no match
            });

            totalGuestsLabel.setText(String.valueOf(filteredData.size())); // update total
        });

    }


    private void loadGuests() {
        List<Guest> guests = guestService.getAllGuests();
        guestData = FXCollections.observableArrayList(guests);
        guests.forEach(g ->
                System.out.println(
                        g.fullName() + " | " + g.email()
                )
        );
        // Wrap in FilteredList
        filteredData = new FilteredList<>(guestData, g -> true);

        guestsTable.setItems(filteredData);
        totalGuestsLabel.setText(String.valueOf(filteredData.size()));
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
                    Guest guest = getTableView().getItems().get(getIndex());
                    openEditGuestPopup(guest);
                });

                deleteBtn.setOnAction(e -> {
                    Guest guest = getTableView().getItems().get(getIndex());
                    deleteGuest(guest.id());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void deleteGuest(UUID guestId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Guest");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This guest will be permanently deleted.");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                guestService.deleteGuest(guestId);
                loadGuests();
            }
        });
    }


    @FXML
    private void openAddGuestPopup() {
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

            guestsTable.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openEditGuestPopup(Guest guest) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/management/system/UpdateGuest.fxml")
            );

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Update Guest");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            UpdateGuestController controller = loader.getController();
            controller.setGuest(guest);

            stage.setOnHidden(e -> loadGuests());

            stage.showAndWait();

            guestsTable.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
