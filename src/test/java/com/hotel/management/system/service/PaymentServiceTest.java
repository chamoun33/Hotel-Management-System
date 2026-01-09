package com.hotel.management.system.service;

import com.hotel.management.system.model.*;
import com.hotel.management.system.repository.IPaymentRepository;
import com.hotel.management.system.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private IPaymentRepository paymentRepository;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository();
        paymentService = new PaymentService(paymentRepository);

        Guest guest = new Guest(UUID.randomUUID(), "luke_john_doe", "luke_john@example.com", null);
        Room room = new Room(101, 2, RoomType.DOUBLE, 100.0, RoomStatus.AVAILABLE);
        testReservation = new Reservation(UUID.randomUUID(), guest, room,
                LocalDate.now(), LocalDate.now().plusDays(2), ReservationStatus.CONFIRMED);
    }

    @Test
    void makePayment_ShouldCreateAndSavePayment() {
        Payment payment = paymentService.makePayment(testReservation, 200.0, PaymentMethod.CASH);

        List<Payment> allPayments = paymentService.getAllPayments();
        assertEquals(1, allPayments.size());

        Payment savedPayment = allPayments.getFirst();
        assertEquals(testReservation, savedPayment.getReservation());
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
        paymentService.makePayment(testReservation,
                200.0, PaymentMethod.CARD); // today
        paymentService.makePayment(testReservation,
                150.0, PaymentMethod.CASH); // today

        // Payment from yesterday
        Payment yesterdayPayment = new Payment(UUID.randomUUID(), testReservation,
                LocalDateTime.of(today.minusDays(1), LocalTime.of(10, 0)),
                100.0, PaymentStatus.PAID, PaymentMethod.CARD);
        paymentRepository.save(yesterdayPayment);

        double revenue = paymentService.getTodayRevenue(today);
        assertEquals(350.0, revenue, 0.01);
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
