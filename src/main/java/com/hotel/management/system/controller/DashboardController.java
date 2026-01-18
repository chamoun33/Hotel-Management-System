package com.hotel.management.system.controller;

import com.hotel.management.system.MainController;
import com.hotel.management.system.database.DB;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.PaymentRepository;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.service.PaymentService;
import com.hotel.management.system.service.ReservationService;
import com.hotel.management.system.service.RoomService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class DashboardController {

    @FXML private Label totalRoomsLabel;
    @FXML private Label occupiedRoomsLabel;
    @FXML private Label availableRoomsLabel;
    @FXML private Label revenueLabel;
    @FXML private Label CheckinsLabel;
    @FXML private Label CheckoutsLabel;
    @FXML private Label maintenanceLabel;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private RoomService roomService;
    private ReservationService reservationService;
    private PaymentService paymentService;

    public void initialize() {
        // Initialize services
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

        loadDashboard();

    }

    public void loadDashboard() {
        totalRoomsLabel.setText(String.valueOf(roomService.getAvailableRoomsCount() + roomService.getOccupiedRoomsCount() + roomService.getRoomsUnderMaintenanceCount()));
        occupiedRoomsLabel.setText(String.valueOf(roomService.getOccupiedRoomsCount()));
        availableRoomsLabel.setText(String.valueOf(roomService.getAvailableRoomsCount()));
        revenueLabel.setText(String.valueOf(paymentService.getTodayRevenue(LocalDate.now())));

        CheckinsLabel.setText(String.valueOf(reservationService.getCheckInsToday(LocalDate.now())) + " room");
        CheckoutsLabel.setText(String.valueOf(reservationService.getCheckOutsToday(LocalDate.now())) + " room");
        maintenanceLabel.setText(String.valueOf(roomService.getRoomsUnderMaintenanceCount())+ " room");
    }

    @FXML
    private void openCheckInPage() {
        if(mainController != null){
            mainController.loadContent("CheckIn.fxml");
        }
    }

    @FXML
    private void openCheckOutPage() {
        if(mainController != null){
            mainController.loadContent("CheckOut.fxml");
        }
    }

    @FXML
    public void openMaintenancePopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/hotel/management/system/MaintenancePopup.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Maintenance Rooms");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> {
                loadDashboard();
                if (mainController != null) {
                    mainController.loadDashboardData();
                }
            });
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
