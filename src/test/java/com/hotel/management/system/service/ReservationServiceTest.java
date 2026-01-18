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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private IGuestRepository guestRepository;
    private IRoomRepository roomRepository;
    private IReservationRepository reservationRepository;
    private ReservationService reservationService;

    private Guest testGuest;
    private Room testRoom;

    private final ConnectionProvider connectionProvider = TestDB.INSTANCE;

    @BeforeEach
    void setUp() throws Exception {
        // Clean tables before each test
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            // Delete child tables first
            stmt.execute("DELETE FROM payments");      // if your test DB has payments
            stmt.execute("DELETE FROM reservations");  // then reservations
            // Delete parent tables after
            stmt.execute("DELETE FROM guests");
            stmt.execute("DELETE FROM rooms");
        }

        reservationRepository = new ReservationRepository(connectionProvider);
        // Inject test DB into repositories
        guestRepository = new GuestRepository(connectionProvider);
        roomRepository = new RoomRepository(connectionProvider);
        IReservationRepository reservationRepository = new ReservationRepository(connectionProvider);

        RoomService roomService = new RoomService(roomRepository, reservationRepository);
        reservationService = new ReservationService(reservationRepository, roomRepository, guestRepository, roomService);

        // Create test guest and room
        testGuest = new Guest(UUID.randomUUID(), "luke_test",
                "luke" + UUID.randomUUID() + "@example.com", null);
        guestRepository.save(testGuest);

        testRoom = new Room(101, 2, RoomType.DOUBLE, 100.0, RoomStatus.AVAILABLE);
        roomRepository.save(testRoom);
    }

    @Test
    void createReservation_WhenRoomAvailable_ShouldCreateReservation() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = LocalDate.now().plusDays(2);

        Reservation reservation = reservationService.createReservation(
                testGuest.id(), testRoom.getRoomNumber(), checkIn, checkOut
        );

        assertNotNull(reservation);
        assertEquals(testGuest, reservation.getGuest());
        assertEquals(testRoom, reservation.getRoom());
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(checkIn, reservation.getCheckIn());
        assertEquals(checkOut, reservation.getCheckOut());
    }

    @Test
    void cancelReservation_ShouldUpdateStatus() {
        Reservation reservation = reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                LocalDate.now(), LocalDate.now().plusDays(2));

        reservationService.cancelReservation(reservation.getId());

        // Fetch the updated reservation from the service
        Reservation updatedReservation = reservationService.getAllReservations().stream()
                .filter(r -> r.getId().equals(reservation.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(ReservationStatus.CANCELLED, updatedReservation.getStatus());
    }

    @Test
    void getReservationsByGuest_ShouldReturnOnlyThatGuestsReservations() {
        Reservation r1 = reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                LocalDate.now(), LocalDate.now().plusDays(2));

        Guest otherGuest = new Guest(UUID.randomUUID(), "other_test",
                "other" + UUID.randomUUID() + "@example.com", null);
        guestRepository.save(otherGuest);
        Room room2 = new Room(102, 2, RoomType.DOUBLE, 120.0, RoomStatus.AVAILABLE);
        roomRepository.save(room2);
        Reservation r2 = reservationService.createReservation(otherGuest.id(), room2.getRoomNumber(),
                LocalDate.now(), LocalDate.now().plusDays(2));

        List<Reservation> result = reservationService.getReservationsByGuest(testGuest.id());
        assertEquals(1, result.size());
        assertTrue(result.contains(r1));
        assertFalse(result.contains(r2));
    }

    @Test
    void checkIn_ShouldUpdateStatusAndRoom() {
        Reservation reservation = reservationService.createReservation(
                testGuest.id(), testRoom.getRoomNumber(),
                LocalDate.now(), LocalDate.now().plusDays(2)
        );

        reservationService.checkIn(reservation.getId());

        // Fetch updated reservation and room from DB
        Reservation updatedReservation = reservationRepository.findById(reservation.getId())
                .orElseThrow();
        Room updatedRoom = roomRepository.findByNumber(testRoom.getRoomNumber())
                .orElseThrow();

        assertEquals(ReservationStatus.CHECKED_IN, updatedReservation.getStatus());
        assertEquals(RoomStatus.OCCUPIED, updatedRoom.getStatus());
    }

    @Test
    void checkOut_ShouldUpdateStatusAndRoom() {
        Reservation reservation = reservationService.createReservation(
                testGuest.id(), testRoom.getRoomNumber(),
                LocalDate.now(), LocalDate.now().plusDays(2)
        );

        reservationService.checkIn(reservation.getId());
        reservationService.checkOut(reservation.getId());

        // Fetch updated reservation and room from DB
        Reservation updatedReservation = reservationRepository.findById(reservation.getId())
                .orElseThrow();
        Room updatedRoom = roomRepository.findByNumber(testRoom.getRoomNumber())
                .orElseThrow();

        assertEquals(ReservationStatus.CHECKED_OUT, updatedReservation.getStatus());
        assertEquals(RoomStatus.MAINTENANCE, updatedRoom.getStatus());
    }

    @Test
    void getNumberOfNights_ShouldReturnCorrectDays() {
        LocalDate checkIn = LocalDate.of(2026, 1, 10);
        LocalDate checkOut = LocalDate.of(2026, 1, 15);
        Reservation reservation = reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                checkIn, checkOut);

        long nights = reservationService.getNumberOfNights(reservation.getId());
        assertEquals(5, nights);
    }

    @Test
    void getOccupiedRooms_ShouldCountCorrectly() {
        LocalDate today = LocalDate.now();
        reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                today.minusDays(1), today.plusDays(2));

        long occupied = reservationService.getOccupiedRooms(today);
        assertEquals(1, occupied);
    }

    @Test
    void getCheckInsToday_ShouldReturnCorrectCount() {
        LocalDate today = LocalDate.now();
        reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                today, today.plusDays(2));

        long count = reservationService.getCheckInsToday(today);
        assertEquals(1, count);
    }

    @Test
    void getCheckOutsToday_ShouldReturnCorrectCount() {
        LocalDate today = LocalDate.now();
        Reservation reservation = reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                today.minusDays(2), today);
        reservationService.checkIn(reservation.getId());

        long count = reservationService.getCheckOutsToday(today);
        assertEquals(1, count);
    }
}
