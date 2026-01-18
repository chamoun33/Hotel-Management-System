package com.hotel.management.system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB implements ConnectionProvider {

    private static final String URL = "jdbc:mysql://localhost:3306/HotelManagementSystem?serverTimezone=UTC&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "@DavidO24#001[MySQL]";

    private TestDB() {} // prevent instantiation

    public static final TestDB INSTANCE = new TestDB(); // singleton instance

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
