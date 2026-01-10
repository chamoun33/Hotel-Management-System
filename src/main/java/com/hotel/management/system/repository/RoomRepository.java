package com.hotel.management.system.repository;

import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.model.*;

import java.sql.*;
import java.util.*;

public class RoomRepository implements IRoomRepository {

    private final ConnectionProvider connectionProvider;

    public RoomRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void save(Room room) {
        String sql = """
            INSERT INTO rooms(room_number, capacity, room_type, price_per_night, status)
            VALUES(?,?,?,?,?)
            ON DUPLICATE KEY UPDATE
            capacity=?, room_type=?, price_per_night=?, status=?
            """;

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            // Insert values
            ps.setInt(1, room.getRoomNumber());
            ps.setInt(2, room.getCapacity());
            ps.setString(3, room.getRoomType().name());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getStatus().name());

            // Update values for duplicate
            ps.setInt(6, room.getCapacity());
            ps.setString(7, room.getRoomType().name());
            ps.setDouble(8, room.getPricePerNight());
            ps.setString(9, room.getStatus().name());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving room", e);
        }
    }

    @Override
    public Optional<Room> findByNumber(int roomNumber) {
        String sql = "SELECT * FROM rooms WHERE room_number=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, roomNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding room", e);
        }
    }

    @Override
    public List<Room> findAll() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT * FROM rooms";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading rooms", e);
        }

        return list;
    }

    @Override
    public void deleteByNumber(int roomNumber) {
        String sql = "DELETE FROM rooms WHERE room_number=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, roomNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting room", e);
        }
    }

    @Override
    public void updateStatus(int roomNumber, RoomStatus status) {
        String sql = "UPDATE rooms SET status=? WHERE room_number=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, roomNumber);

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating room status", e);
        }
    }

    private Room map(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("room_number"),
                rs.getInt("capacity"),
                RoomType.valueOf(rs.getString("room_type")),
                rs.getDouble("price_per_night"),
                RoomStatus.valueOf(rs.getString("status"))
        );
    }
}
