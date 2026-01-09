package com.hotel.management.system.service;

import com.hotel.management.system.model.*;
import com.hotel.management.system.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {

    private IGuestRepository guestRepository;
    private IRoomRepository roomRepository;
    private ReservationService reservationService;

    private Guest testGuest;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        guestRepository = new GuestRepository();
        roomRepository = new RoomRepository();
        IReservationRepository reservationRepository = new ReservationRepository();
        RoomService roomService = new RoomService(roomRepository, reservationRepository);
        reservationService = new ReservationService(reservationRepository, roomRepository, guestRepository, roomService);

        testGuest = new Guest(UUID.randomUUID(), "luke_test", "luke@example.com", null);
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

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void getReservationsByGuest_ShouldReturnOnlyThatGuestsReservations() {
        Reservation r1 = reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                LocalDate.now(), LocalDate.now().plusDays(2));

        Guest otherGuest = new Guest(UUID.randomUUID(), "other_test", "other@example.com", null);
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
        Reservation reservation = reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                LocalDate.now(), LocalDate.now().plusDays(2));

        reservationService.checkIn(reservation.getId());

        assertEquals(ReservationStatus.CHECKED_IN, reservation.getStatus());
        assertEquals(RoomStatus.OCCUPIED, testRoom.getStatus());
    }

    @Test
    void checkOut_ShouldUpdateStatusAndRoom() {
        Reservation reservation = reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(),
                LocalDate.now(), LocalDate.now().plusDays(2));

        reservationService.checkIn(reservation.getId());
        reservationService.checkOut(reservation.getId());

        assertEquals(ReservationStatus.CHECKED_OUT, reservation.getStatus());
        assertEquals(RoomStatus.AVAILABLE, testRoom.getStatus());
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
        reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(), today.minusDays(1), today.plusDays(2));

        long occupied = reservationService.getOccupiedRooms(today);
        assertEquals(1, occupied);
    }

    @Test
    void getCheckInsToday_ShouldReturnCorrectCount() {
        LocalDate today = LocalDate.now();
        reservationService.createReservation(testGuest.id(), testRoom.getRoomNumber(), today, today.plusDays(2));

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
