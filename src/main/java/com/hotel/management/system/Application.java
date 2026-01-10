package com.hotel.management.system;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("MainDashboard.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 840);
        stage.setTitle("Hotel Management System");
        stage.setScene(scene);
        stage.show();
    }
}
