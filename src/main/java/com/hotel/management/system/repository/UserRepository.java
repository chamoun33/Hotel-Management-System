package com.hotel.management.system.repository;

import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.model.*;

import java.sql.*;
import java.util.*;

public class UserRepository implements IUserRepository {

    private final ConnectionProvider connectionProvider;

    public UserRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void save(User user) {
        String sql = """
            INSERT INTO users(id, username, password, role, phone_number, created_at)
            VALUES (?,?,?,?,?,?)
            ON DUPLICATE KEY UPDATE
            password=?, role=?, phone_number=?
            """;

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // Insert values
            ps.setString(1, user.getId().toString());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user.getPhoneNumber() == null ? null : user.getPhoneNumber().toString());
            ps.setTimestamp(6, Timestamp.valueOf(user.getCreated_at()));

            // Update values for duplicate
            ps.setString(7, user.getPassword());
            ps.setString(8, user.getRole().name());
            ps.setString(9, user.getPhoneNumber() == null ? null : user.getPhoneNumber().toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        String sql = "SELECT * FROM users WHERE id=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading users", e);
        }
        return list;
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM users WHERE id=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public void updatePassword(UUID userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, userId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                UUID.fromString(rs.getString("id")),
                rs.getString("username"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role"))
        );
    }
}
