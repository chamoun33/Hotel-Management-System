package com.hotel.management.system.controller;

import com.hotel.management.system.MainController;
import com.hotel.management.system.database.DB;
import com.hotel.management.system.model.*;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.PaymentRepository;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.security.CurrentUser;
import com.hotel.management.system.service.GuestService;
import com.hotel.management.system.service.PaymentService;
import com.hotel.management.system.service.ReservationService;
import com.hotel.management.system.service.RoomService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, String> paymentIdCol;
    @FXML private TableColumn<Payment, Integer> roomNumberCol;
    @FXML private TableColumn<Payment, String> guestNameCol;
    @FXML private TableColumn<Payment, Double> amountCol;
    @FXML private TableColumn<Payment, LocalDate> dateCol;
    @FXML private TableColumn<Payment, LocalDate> receiver;
    @FXML private TableColumn<Payment, String> methodCol;

    @FXML private TabPane tabPane;
    @FXML private Tab paymentsTab;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


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
    private PaymentService paymentService;

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
        paymentService = new PaymentService(
                new PaymentRepository(
                        DB.INSTANCE,
                        new ReservationRepository(DB.INSTANCE)
                )
        );


        paymentIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomNumberCol.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue();

            return reservationService
                    .getReservationById(payment.getReservation().getId())
                    .map(r -> r.getRoom().getRoomNumber())
                    .map(SimpleIntegerProperty::new)
                    .orElse(new SimpleIntegerProperty(0))
                    .asObject();
        });

        guestNameCol.setCellValueFactory(cellData -> {
            Payment payment = cellData.getValue();

            return reservationService
                    .getReservationById(payment.getReservation().getId())
                    .map(r -> resolveGuestName(r.getGuest().id()))
                    .map(SimpleStringProperty::new)
                    .orElse(new SimpleStringProperty("Unknown"));
        });

        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        methodCol.setCellValueFactory(new PropertyValueFactory<>("method"));
        receiver.setCellValueFactory(new PropertyValueFactory<>("receiver"));

        loadPayments();
        loadOccupiedRooms();

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {

            if (newTab == paymentsTab && CurrentUser.get().getRole() != Role.ADMIN) {

                showAuthorizationError();

                // go back to Check-Out tab
                tabPane.getSelectionModel().select(oldTab);
            }
        });

    }

    private void loadPayments() {
        paymentsTable.getItems().setAll(paymentService.getAllPayments());
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
                long nightStayed = reservationService.getNumberOfNightsBetweenDates(newReservation.getCheckIn(), LocalDate.now());
                if(nightStayed == 0){
                    nightStayed = 1;
                }
                nightsStayedLabel.setText(String.valueOf(nightStayed));
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

            stage.setOnHidden(e -> {
                onClear();
                loadOccupiedRooms();
                loadPayments();

                if (mainController != null) {
                    mainController.loadDashboardData();
                }
            });



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

    private void showAuthorizationError() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Authorization Required");
        alert.setHeaderText("Access Denied");
        alert.setContentText(
                "You do not have permission to view payment history.\n" +
                        "Please contact an administrator if you believe this is a mistake."
        );
        alert.showAndWait();
    }

}
