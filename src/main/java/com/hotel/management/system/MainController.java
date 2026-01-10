package com.hotel.management.system;

import com.hotel.management.system.controller.CheckInController;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private StackPane contentArea;
    @FXML
    private Button roomsBtn, bookingBtn, guestsBtn, dashboardBtn, checkoutBtn, checkinBtn;

    public void initialize() {
        // Set up button actions
        dashboardBtn.setOnAction(e -> loadContent("DashboardContent.fxml"));
        roomsBtn.setOnAction(e -> loadContent("Rooms.fxml"));
        bookingBtn.setOnAction(e -> loadContent("Booking.fxml"));
        guestsBtn.setOnAction(e -> loadContent("Guests.fxml"));
        checkinBtn.setOnAction(e -> loadContent("CheckIn.fxml"));
        checkoutBtn.setOnAction(e -> loadContent("CheckOut.fxml"));
    }

    private void loadContent(String fxmlFile) {
        try {
            // Clear current content
            contentArea.getChildren().clear();

            // Load new content
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent content = loader.load();

            Object controller = loader.getController();
            if (controller instanceof CheckInController checkInController) {
                checkInController.setMainController(this);
            }

            contentArea.getChildren().add(content);

            // Update active button style
            updateActiveButton(fxmlFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void navigateTo(String fxmlFile) {
        loadContent(fxmlFile);
    }


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
}
