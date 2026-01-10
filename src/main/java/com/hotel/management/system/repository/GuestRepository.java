package com.hotel.management.system.repository;

import com.hotel.management.system.database.ConnectionProvider;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import com.hotel.management.system.model.Guest;

import java.sql.*;
import java.util.*;

public class GuestRepository implements IGuestRepository {

    private final ConnectionProvider connectionProvider;

    public GuestRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void save(Guest guest) {
        String sql = "INSERT INTO guests(id, full_name, email, phone_number) VALUES(?,?,?,?)";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, guest.id().toString());
            ps.setString(2, guest.fullName());
            ps.setString(3, guest.email());
            ps.setString(
                    4,
                    guest.phoneNumber() == null
                            ? null
                            : "+" + guest.phoneNumber().getCountryCode()
                            + guest.phoneNumber().getNationalNumber()
            );

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving guest", e);
        }
    }

    @Override
    public Optional<Guest> findById(UUID id) {
        String sql = "SELECT * FROM guests WHERE id=?";

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String rawPhone = rs.getString("phone_number");
                PhoneNumber phone = null;

                if (rawPhone != null && !rawPhone.isBlank()) {
                    try {
                        phone = phoneUtil.parse(rawPhone, null);
                    } catch (NumberParseException e) {
                        // Log but don't break the UI
                        System.err.println("Invalid phone in DB: " + rawPhone);
                    }
                }

                return Optional.of(new Guest(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        phone
                ));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding guest", e);
        }
    }

    @Override
    public List<Guest> findAll() {

        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests";

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String rawPhone = rs.getString("phone_number");
                PhoneNumber phone = null;

                if (rawPhone != null && !rawPhone.isBlank()) {
                    try {
                        phone = phoneUtil.parse(rawPhone, null);
                    } catch (NumberParseException e) {
                        // Log but don't break the UI
                        System.err.println("Invalid phone in DB: " + rawPhone);
                    }
                }

                list.add(new Guest(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        phone
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error loading guests", e);
        }

        return list;
    }


    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM guests WHERE id=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting guest", e);
        }
    }

    @Override
    public void update(Guest guest) {
        String sql = "UPDATE guests SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, guest.fullName());
            ps.setString(2, guest.email());
            ps.setString(
                    3,
                    guest.phoneNumber() == null
                            ? null
                            : "+" + guest.phoneNumber().getCountryCode()
                            + guest.phoneNumber().getNationalNumber()
            );
            ps.setString(4, guest.id().toString()); // WHERE clause

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("No guest found with id: " + guest.id());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating guest", e);
        }
    }

}
