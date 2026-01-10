package com.hotel.management.system.service;

import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.database.TestDB;
import com.hotel.management.system.model.*;
import com.hotel.management.system.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private IPaymentRepository paymentRepository;

    private Reservation testReservation;

    private final ConnectionProvider connectionProvider = TestDB.INSTANCE;

    @BeforeEach
    void setUp() throws Exception {
        // Clean DB tables in correct order to satisfy foreign keys
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM payments");
            stmt.execute("DELETE FROM reservations");
            stmt.execute("DELETE FROM guests");
            stmt.execute("DELETE FROM rooms");
        }

        // Initialize repositories
        IGuestRepository guestRepository = new GuestRepository(connectionProvider);
        IRoomRepository roomRepository = new RoomRepository(connectionProvider);
        IReservationRepository reservationRepository = new ReservationRepository(connectionProvider);
        paymentRepository = new PaymentRepository(connectionProvider, reservationRepository);

        // Initialize services
        paymentService = new PaymentService(paymentRepository);

        // Create test guest and room and save them
        Guest testGuest = new Guest(UUID.randomUUID(), "luke_john_doe",
                "luke_john" + UUID.randomUUID() + "@example.com", null);
        guestRepository.save(testGuest);

        Room testRoom = new Room(101, 2, RoomType.DOUBLE, 100.0, RoomStatus.AVAILABLE);
        roomRepository.save(testRoom);

        // Create and save test reservation
        testReservation = new Reservation(UUID.randomUUID(), testGuest, testRoom,
                LocalDate.now(), LocalDate.now().plusDays(2), ReservationStatus.CONFIRMED);
        reservationRepository.save(testReservation);
    }

    @Test
    void makePayment_ShouldCreateAndSavePayment() {
        Payment payment = paymentService.makePayment(testReservation, 200.0, PaymentMethod.CASH);

        List<Payment> allPayments = paymentService.getAllPayments();
        assertEquals(1, allPayments.size());

        Payment savedPayment = allPayments.getFirst();
        assertEquals(testReservation.getId(), savedPayment.getReservation().getId());
        assertEquals(200.0, savedPayment.getAmount());
        assertEquals(PaymentMethod.CASH, savedPayment.getMethod());
        assertEquals(PaymentStatus.PAID, savedPayment.getStatus());
        assertNotNull(savedPayment.getId());
        assertNotNull(savedPayment.getPaymentDate());
        assertEquals(payment, savedPayment);
    }

    @Test
    void getPayment_WhenPaymentExists_ShouldReturnPayment() {
        Payment payment = paymentService.makePayment(testReservation, 150.0, PaymentMethod.CARD);

        Optional<Payment> fetched = paymentService.getPayment(payment.getId());
        assertTrue(fetched.isPresent());
        assertEquals(payment, fetched.get());
    }

    @Test
    void getPayment_WhenPaymentDoesNotExist_ShouldReturnEmpty() {
        Optional<Payment> fetched = paymentService.getPayment(UUID.randomUUID());
        assertFalse(fetched.isPresent());
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() {
        Payment p1 = paymentService.makePayment(testReservation, 200.0, PaymentMethod.CARD);
        Payment p2 = paymentService.makePayment(testReservation, 150.0, PaymentMethod.ONLINE);

        List<Payment> allPayments = paymentService.getAllPayments();
        assertEquals(2, allPayments.size());
        assertTrue(allPayments.contains(p1));
        assertTrue(allPayments.contains(p2));
    }

    @Test
    void getTodayRevenue_ShouldCalculateCorrectRevenue() {
        LocalDate today = LocalDate.now();
        paymentService.makePayment(testReservation, 200.0, PaymentMethod.CARD); // today
        paymentService.makePayment(testReservation, 150.0, PaymentMethod.CASH); // today

        // Payment from yesterday
        Payment yesterdayPayment = new Payment(UUID.randomUUID(), testReservation,
                LocalDateTime.of(today.minusDays(1), LocalTime.of(10, 0)),
                100.0, PaymentStatus.PAID, PaymentMethod.CARD);
        paymentRepository.save(yesterdayPayment);

        double revenue = paymentService.getTodayRevenue(today);
        assertEquals(350.0, revenue, 0.01); // 200 + 150
    }

    @Test
    void getTodayRevenue_WhenNoPaymentsToday_ShouldReturnZero() {
        LocalDate today = LocalDate.now();

        // Payment from yesterday
        Payment yesterdayPayment = new Payment(UUID.randomUUID(), testReservation,
                LocalDateTime.of(today.minusDays(1), LocalTime.of(10, 0)),
                100.0, PaymentStatus.PAID, PaymentMethod.CARD);
        paymentRepository.save(yesterdayPayment);

        double revenue = paymentService.getTodayRevenue(today);
        assertEquals(0.0, revenue, 0.01);
    }

    @Test
    void getTodayRevenue_WhenNoPaymentsAtAll_ShouldReturnZero() {
        double revenue = paymentService.getTodayRevenue(LocalDate.now());
        assertEquals(0.0, revenue, 0.01);
    }
}
