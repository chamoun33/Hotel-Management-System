package com.hotel.management.system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB implements ConnectionProvider {

    private static final String URL = "jdbc:mysql://localhost:3306/hotelManagementSystemTest?serverTimezone=UTC&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "admin123";

    private TestDB() {} // prevent instantiation

    public static final TestDB INSTANCE = new TestDB(); // singleton instance

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
