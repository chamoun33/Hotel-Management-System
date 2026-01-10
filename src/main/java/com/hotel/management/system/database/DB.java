package com.hotel.management.system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB implements ConnectionProvider {

    private static final String URL = "jdbc:mysql://localhost:3306/hotelManagementSystem?serverTimezone=UTC&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "admin123";

    private DB() {} // prevent instantiation

    public static final DB INSTANCE = new DB(); // singleton instance

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
