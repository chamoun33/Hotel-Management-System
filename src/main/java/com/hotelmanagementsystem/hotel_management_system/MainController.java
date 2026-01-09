package com.hotelmanagementsystem.hotel_management_system;

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
    private Button roomsBtn, bookingBtn, guestsBtn, dashboardBtn;

    public void initialize() {
        // Set up button actions
        dashboardBtn.setOnAction(e -> loadContent("DashboardContent.fxml"));
        roomsBtn.setOnAction(e -> loadContent("Rooms.fxml"));
        bookingBtn.setOnAction(e -> loadContent("booking.fxml"));
        guestsBtn.setOnAction(e -> loadContent("Guests.fxml"));
    }

    private void loadContent(String fxmlFile) {
        try {
            // Clear current content
            contentArea.getChildren().clear();

            // Load new content
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent content = loader.load();
            contentArea.getChildren().add(content);

            // Update active button style
            updateActiveButton(fxmlFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateActiveButton(String fxmlFile) {
        // Reset all buttons to inactive style
        dashboardBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
        roomsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
        // ... others

        // Set active button style
        if (fxmlFile.contains("Dashboard")) {
            dashboardBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        } else if (fxmlFile.contains("Rooms")) {
            roomsBtn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white;");
        }
        // ... others
    }
}
