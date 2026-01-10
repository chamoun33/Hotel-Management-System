module com.hotelmanagementsystem.hotel_management_system {
    requires javafx.controls;
    requires javafx.fxml;
    requires libphonenumber;
    requires java.sql;
    requires mysql.connector.j;
    requires javafx.graphics;
    requires javafx.base;


    opens com.hotel.management.system to javafx.fxml;

    opens com.hotel.management.system.controller to javafx.fxml;
    opens com.hotel.management.system.model to javafx.base;

    exports com.hotel.management.system;

}