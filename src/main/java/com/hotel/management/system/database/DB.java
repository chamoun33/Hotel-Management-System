package com.hotel.management.system.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB implements ConnectionProvider {

    private static final String URL = "jdbc:mysql://localhost:3306/HotelManagementSystem";
    private static final String USER = "root";
    private static final String PASSWORD = "@DavidO24#001[MySQL]";

    private DB() {}

    public static final DB INSTANCE = new DB();

    @Override
    public Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain DB connection", e);
        }
    }
}

