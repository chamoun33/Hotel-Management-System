module com.hotelmanagementsystem.hotel_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires libphonenumber;
    requires java.sql;


    opens com.hotel.management.system to javafx.fxml;
    exports com.hotel.management.system;
}