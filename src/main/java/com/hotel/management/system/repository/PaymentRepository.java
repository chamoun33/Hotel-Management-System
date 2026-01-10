package com.hotel.management.system.repository;

import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.model.*;

import java.sql.*;
import java.util.*;

public class PaymentRepository implements IPaymentRepository {

    private final ConnectionProvider connectionProvider;
    private final IReservationRepository reservationRepository;

    // Constructor injection for connection provider and reservation repo
    public PaymentRepository(ConnectionProvider connectionProvider, IReservationRepository reservationRepository) {
        this.connectionProvider = connectionProvider;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void save(Payment payment) {
        // Ensure a reservation exists, only if not already in DB
        if (payment.getReservation() != null) {
            UUID resId = payment.getReservation().getId();
            if (reservationRepository.findById(resId).isEmpty()) {
                reservationRepository.save(payment.getReservation());
            }
        }

        String sql = """
        INSERT INTO payments
        (id, reservation_id, payment_date, amount, status, method)
        VALUES (?,?,?,?,?,?)
    """;

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, payment.getId().toString());
            ps.setString(2, payment.getReservation() == null ? null : payment.getReservation().getId().toString());
            ps.setTimestamp(3, payment.getPaymentDate() == null ? null : Timestamp.valueOf(payment.getPaymentDate()));
            ps.setDouble(4, payment.getAmount());
            ps.setString(5, payment.getStatus().name());
            ps.setString(6, payment.getMethod().name());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        String sql = "SELECT * FROM payments WHERE id=?";
        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payment", e);
        }
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading payments", e);
        }

        return list;
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM payments WHERE id=?";

        try (Connection c = connectionProvider.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting payment", e);
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        UUID reservationId = UUID.fromString(rs.getString("reservation_id"));
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);

        return new Payment(
                UUID.fromString(rs.getString("id")),
                reservation,
                rs.getTimestamp("payment_date") == null ? null : rs.getTimestamp("payment_date").toLocalDateTime(),
                rs.getDouble("amount"),
                PaymentStatus.valueOf(rs.getString("status")),
                PaymentMethod.valueOf(rs.getString("method"))
        );
    }
}
