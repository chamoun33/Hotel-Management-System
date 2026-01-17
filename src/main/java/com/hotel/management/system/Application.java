package com.hotel.management.system;

import com.hotel.management.system.security.CurrentUser;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        Image icon = new Image(getClass().getResourceAsStream("/assets/Images/icons8-hotel-64.png"));
        stage.getIcons().add(icon);
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("Login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Hotel Management System");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
//        stage.setOnCloseRequest(event -> {
//            event.consume(); // prevent auto close
//            confirmExit(stage);
//        });
        stage.show();
    }

//    private void confirmExit(Stage stage) {
//        Alert alert = new Alert(
//                Alert.AlertType.CONFIRMATION,
//                "Are you sure you want to exit?",
//                ButtonType.YES,
//                ButtonType.NO
//        );
//
//        alert.showAndWait().ifPresent(btn -> {
//            if (btn == ButtonType.YES) {
//                CurrentUser.clear();
//                stage.close();
//            }
//        });
//    }

}
