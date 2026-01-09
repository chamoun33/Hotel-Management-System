module com.hotelmanagementsystem.hotel_management_system {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.hotelmanagementsystem.hotel_management_system to javafx.fxml;
    exports com.hotelmanagementsystem.hotel_management_system;
}