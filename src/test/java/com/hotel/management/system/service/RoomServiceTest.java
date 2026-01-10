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

class RoomServiceTest {

    private RoomService roomService;
    private IReservationRepository reservationRepository;

    private Room testRoom;
    private Guest testGuest;

    private final ConnectionProvider connectionProvider = TestDB.INSTANCE;

    @BeforeEach
    void setUp() throws Exception {
        // Clean tables before each test
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM payments");
            stmt.execute("DELETE FROM reservations");
            stmt.execute("DELETE FROM guests");
            stmt.execute("DELETE FROM rooms");
        }

        // Inject TestDB into repositories
        IGuestRepository guestRepository = new GuestRepository(connectionProvider);
        IRoomRepository roomRepository = new RoomRepository(connectionProvider);
        reservationRepository = new ReservationRepository(connectionProvider);

        roomService = new RoomService(roomRepository, reservationRepository);

        // Create test guest and room
        testGuest = new Guest(UUID.randomUUID(), "luke_john_doe",
                "luke" + UUID.randomUUID() + "@example.com", null);
        guestRepository.save(testGuest);

        testRoom = new Room(101, 2, RoomType.DOUBLE, 100.0, RoomStatus.AVAILABLE);
    }

    @Test
    void addRoom_ShouldSaveRoom() {
        roomService.addRoom(testRoom);

        List<Room> allRooms = roomService.getAllRooms();
        assertEquals(1, allRooms.size());
        assertEquals(testRoom, allRooms.getFirst());
    }

    @Test
    void getRoom_WhenRoomExists_ShouldReturnRoom() {
        roomService.addRoom(testRoom);

        assertTrue(roomService.getRoom(101).isPresent());
        assertEquals(testRoom, roomService.getRoom(101).get());
    }

    @Test
    void getRoom_WhenRoomDoesNotExist_ShouldReturnEmpty() {
        assertTrue(roomService.getRoom(999).isEmpty());
    }

    @Test
    void getAllRooms_ShouldReturnAllRooms() {
        Room room2 = new Room(102, 4, RoomType.SUITE, 200.0, RoomStatus.AVAILABLE);
        roomService.addRoom(testRoom);
        roomService.addRoom(room2);

        List<Room> rooms = roomService.getAllRooms();
        assertEquals(2, rooms.size());
        assertTrue(rooms.contains(testRoom));
        assertTrue(rooms.contains(room2));
    }

    @Test
    void deleteRoom_ShouldDeleteRoom() {
        roomService.addRoom(testRoom);
        roomService.deleteRoom(101);

        assertTrue(roomService.getAllRooms().isEmpty());
    }

    @Test
    void isRoomAvailable_WhenRoomDoesNotExist_ShouldReturnFalse() {
        boolean available = roomService.isRoomAvailable(999, LocalDate.now(), LocalDate.now().plusDays(2));
        assertFalse(available);
    }

    @Test
    void isRoomAvailable_WhenRoomIsNotAvailable_ShouldReturnFalse() {
        Room occupiedRoom = new Room(101, 2, RoomType.DOUBLE, 100.0, RoomStatus.OCCUPIED);
        roomService.addRoom(occupiedRoom);

        boolean available = roomService.isRoomAvailable(101, LocalDate.now(), LocalDate.now().plusDays(2));
        assertFalse(available);
    }

    @Test
    void isRoomAvailable_WhenNoConflictingReservations_ShouldReturnTrue() {
        roomService.addRoom(testRoom);

        boolean available = roomService.isRoomAvailable(101, LocalDate.now(), LocalDate.now().plusDays(2));
        assertTrue(available);
    }

    @Test
    void isRoomAvailable_WhenReservationExistsButCancelled_ShouldReturnTrue() {
        roomService.addRoom(testRoom);
        Reservation cancelledReservation = new Reservation(UUID.randomUUID(), testGuest, testRoom,
                LocalDate.now(), LocalDate.now().plusDays(2), ReservationStatus.CANCELLED);
        reservationRepository.save(cancelledReservation);

        boolean available = roomService.isRoomAvailable(101, LocalDate.now(), LocalDate.now().plusDays(2));
        assertTrue(available);
    }

    @Test
    void isRoomAvailable_WhenReservationOverlaps_ShouldReturnFalse() {
        roomService.addRoom(testRoom);
        Reservation existingReservation = new Reservation(UUID.randomUUID(), testGuest, testRoom,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), ReservationStatus.CONFIRMED);
        reservationRepository.save(existingReservation);

        boolean available = roomService.isRoomAvailable(101, LocalDate.now(), LocalDate.now().plusDays(2));
        assertFalse(available);
    }

    @Test
    void getAvailableRooms_ShouldReturnOnlyAvailableRooms() {
        Room availableRoom = new Room(101, 2, RoomType.DOUBLE, 100.0, RoomStatus.AVAILABLE);
        Room occupiedRoom = new Room(102, 2, RoomType.DOUBLE, 100.0, RoomStatus.OCCUPIED);

        roomService.addRoom(availableRoom);
        roomService.addRoom(occupiedRoom);

        List<Room> availableRooms = roomService.getAvailableRooms(LocalDate.now(), LocalDate.now().plusDays(2));
        assertEquals(1, availableRooms.size());
        assertTrue(availableRooms.contains(availableRoom));
        assertFalse(availableRooms.contains(occupiedRoom));
    }

    @Test
    void getRoomsUnderMaintenance_ShouldReturnCorrectCount() {
        Room maintenanceRoom1 = new Room(101, 2, RoomType.DOUBLE, 100.0, RoomStatus.MAINTENANCE);
        Room maintenanceRoom2 = new Room(102, 2, RoomType.DOUBLE, 100.0, RoomStatus.MAINTENANCE);
        Room availableRoom = new Room(103, 2, RoomType.DOUBLE, 100.0, RoomStatus.AVAILABLE);

        roomService.addRoom(maintenanceRoom1);
        roomService.addRoom(maintenanceRoom2);
        roomService.addRoom(availableRoom);

        long count = roomService.getRoomsUnderMaintenance();
        assertEquals(2, count);
    }

    @Test
    void getRoomsUnderMaintenance_WhenNoRoomsUnderMaintenance_ShouldReturnZero() {
        roomService.addRoom(testRoom);
        long count = roomService.getRoomsUnderMaintenance();
        assertEquals(0, count);
    }
}
