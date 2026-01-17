package com.hotel.management.system;

import com.hotel.management.system.controller.*;
import com.hotel.management.system.database.DB;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.PaymentRepository;
import com.hotel.management.system.repository.ReservationRepository;
import com.hotel.management.system.repository.RoomRepository;
import com.hotel.management.system.security.CurrentUser;
import com.hotel.management.system.service.PaymentService;
import com.hotel.management.system.service.ReservationService;
import com.hotel.management.system.service.RoomService;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class MainController {
    @FXML
    private StackPane contentArea;
    @FXML
    private Button roomsBtn, bookingBtn, guestsBtn, dashboardBtn, checkoutBtn, checkinBtn;
    @FXML
    private Label sidebarAvailableLabel, sidebarOccupiedLabel, sidebarMaintenanceLabel, welcomeLabel, roleLabel;
    @FXML
    private Button profileBtn;

    private ContextMenu profileMenu;


    private RoomService roomService;
    private ReservationService reservationService;
    private PaymentService paymentService;

    public void initialize() {
        // Set up button actions
        dashboardBtn.setOnAction(e -> loadContent("DashboardContent.fxml"));
        roomsBtn.setOnAction(e -> loadContent("Rooms.fxml"));
        bookingBtn.setOnAction(e -> loadContent("Booking.fxml"));
        guestsBtn.setOnAction(e -> loadContent("Guests.fxml"));
        checkinBtn.setOnAction(e -> loadContent("CheckIn.fxml"));
        checkoutBtn.setOnAction(e -> loadContent("CheckOut.fxml"));
        welcomeLabel.setText("Welcome, "+String.valueOf(CurrentUser.get().getUsername()));

        if(CurrentUser.isAdmin()){
            roleLabel.setText("Administrator");
        }
        else {
            roleLabel.setText("Staff");
        }

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

        loadDashboardData();
        setupProfileMenu();

    }

    public void loadDashboardData() {
        sidebarAvailableLabel.setText("Available: "+String.valueOf(roomService.getAvailableRoomsCount()));
        sidebarOccupiedLabel.setText("Occupied: "+String.valueOf(roomService.getOccupiedRoomsCount()));
        sidebarMaintenanceLabel.setText("Maintenance: "+String.valueOf(roomService.getRoomsUnderMaintenanceCount()));
    }


    private void loadContent(String fxmlFile) {
        try {
            contentArea.getChildren().clear();

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent content = loader.load();

            Object controller = loader.getController();

            if (controller instanceof CheckInController c) {
                c.setMainController(this);
            }
            else if (controller instanceof RoomController c) {
                c.setMainController(this);
            }
            else if (controller instanceof CheckOutController c){
                c.setMainController(this);
            }

            contentArea.getChildren().add(content);
            updateActiveButton(fxmlFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupProfileMenu() {

        MenuItem changePassword = new MenuItem("Change Password");
        changePassword.setOnAction(e -> openChangePasswordDialog());

        MenuItem userManagement = new MenuItem("User Management");
        userManagement.setOnAction(e -> loadContent("UsersManagement.fxml"));

        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(e -> logout());

        profileMenu = new ContextMenu();

        // ROLE CHECK
        if (CurrentUser.isAdmin()) {
            profileMenu.getItems().addAll(
                    changePassword,
                    userManagement,
                    new SeparatorMenuItem(),
                    logout
            );
        } else {
            profileMenu.getItems().addAll(
                    changePassword,
                    new SeparatorMenuItem(),
                    logout
            );
        }

        profileBtn.setOnAction(e -> {
            if (!profileMenu.isShowing()) {
                profileMenu.show(profileBtn, Side.BOTTOM, 0, 0);
            } else {
                profileMenu.hide();
            }
        });
    }

    private void logout() {
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to logout?",
                ButtonType.YES,
                ButtonType.NO
        );

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                CurrentUser.clear();
                showLoginScreen();
            }
        });
    }

    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/management/system/Login.fxml")
            );

            Stage stage = (Stage) profileBtn.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Hotel Management System");
            stage.setResizable(false);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public void navigateTo(String fxmlFile) {
        loadContent(fxmlFile);
    }
    public void LoadRoomsNumber() {loadDashboardData();}


    private void updateActiveButton(String fxmlFile) {
        // Reset all buttons to inactive style
        dashboardBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        roomsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        bookingBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        guestsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        checkinBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        checkoutBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");

        // Set active button style
        if (fxmlFile.contains("Dashboard")) {
            dashboardBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        } else if (fxmlFile.contains("Rooms")) {
            roomsBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        } else if (fxmlFile.contains("Booking")) {
            bookingBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        } else if (fxmlFile.contains("Guests")) {
            guestsBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        } else if (fxmlFile.contains("CheckIn")) {
            checkinBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        } else if (fxmlFile.contains("CheckOut")) {
            checkoutBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 5; -fx-alignment:  CENTER_LEFT; -fx-padding:  12 20;");
        }
    }

    private void openChangePasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/management/system/ChangePassword.fxml")
            );

            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Change Password");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.initOwner(profileBtn.getScene().getWindow());

            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
