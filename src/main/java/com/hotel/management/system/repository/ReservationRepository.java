package com.hotel.management.system.repository;

import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.model.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ReservationRepository implements IReservationRepository {

    private final ConnectionProvider connectionProvider;

    // Constructor injection
    public ReservationRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public void save(Reservation reservation) {
        String sql = """
        INSERT INTO reservations
        (id, guest_id, room_number, created_at, check_in, check_out, status)
        VALUES (?,?,?,?,?,?,?)
        ON DUPLICATE KEY UPDATE
        status = VALUES(status)
    """;

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, reservation.getId().toString());
            ps.setString(2, reservation.getGuest().id().toString());
            ps.setInt(3, reservation.getRoom().getRoomNumber());
            ps.setTimestamp(4, reservation.getCreated_at() == null ? new Timestamp(System.currentTimeMillis()) : Timestamp.valueOf(reservation.getCreated_at()));
            ps.setDate(5, Date.valueOf(reservation.getCheckIn()));
            ps.setDate(6, Date.valueOf(reservation.getCheckOut()));
            ps.setString(7, reservation.getStatus().name());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving reservation", e);
        }
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        String sql = "SELECT * FROM reservations WHERE id=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservation", e);
        }
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading reservations", e);
        }

        return list;
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM reservations WHERE id=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting reservation", e);
        }
    }

    @Override
    public void updateStatus(UUID reservationId, ReservationStatus status) {
        String sql = "UPDATE reservations SET status=? WHERE id=?";
        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, reservationId.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating reservation status", e);
        }
    }

    private Reservation map(ResultSet rs) throws SQLException {
        // Lazy load guest and room with only IDs
        Guest guest = new Guest(
                UUID.fromString(rs.getString("guest_id")),
                null,
                null,
                null
        );

        Room room = new Room(
                rs.getInt("room_number"),
                0,
                null,
                0.0,
                null
        );

        return new Reservation(
                UUID.fromString(rs.getString("id")),
                guest,
                room,
                rs.getDate("check_in").toLocalDate(),
                rs.getDate("check_out").toLocalDate(),
                ReservationStatus.valueOf(rs.getString("status"))
        );
    }
}
