package com.hotel.management.system.service;

import com.hotel.management.system.database.ConnectionProvider;
import com.hotel.management.system.database.TestDB;
import com.hotel.management.system.model.Guest;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.IGuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GuestServiceTest {

    private GuestService guestService;
    private Guest testGuest;
    private UUID testGuestId;

    // Use the TestDB as a ConnectionProvider instance
    private final ConnectionProvider connectionProvider = TestDB.INSTANCE;

    @BeforeEach
    void setUp() throws Exception {
        // Clean the test DB before each test
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM payments");
            stmt.execute("DELETE FROM reservations");
            stmt.execute("DELETE FROM guests");
        }

        // Inject the TestDB into the repository
        IGuestRepository guestRepository = new GuestRepository(connectionProvider);
        guestService = new GuestService(guestRepository);

        // Unique guest per test to avoid duplicate email errors
        testGuestId = UUID.randomUUID();
        String uniqueEmail = "john" + UUID.randomUUID() + "@example.com";
        testGuest = new Guest(testGuestId, "John Doe", uniqueEmail, null);
    }

    @Test
    void addGuest_ShouldSaveGuest() {
        guestService.addGuest(testGuest);

        List<Guest> allGuests = guestService.getAllGuests();
        assertEquals(1, allGuests.size());

        Guest saved = allGuests.getFirst();
        assertEquals(testGuestId, saved.id());
        assertEquals(testGuest.fullName(), saved.fullName());
        assertEquals(testGuest.email(), saved.email());
    }

    @Test
    void getGuestById_WhenGuestExists_ShouldReturnGuest() {
        guestService.addGuest(testGuest);

        Optional<Guest> result = guestService.getGuestById(testGuestId);
        assertTrue(result.isPresent());

        Guest saved = result.get();
        assertEquals(testGuestId, saved.id());
        assertEquals(testGuest.fullName(), saved.fullName());
        assertEquals(testGuest.email(), saved.email());
    }

    @Test
    void getGuestById_WhenGuestDoesNotExist_ShouldReturnEmpty() {
        Optional<Guest> result = guestService.getGuestById(testGuestId);
        assertFalse(result.isPresent());
    }

    @Test
    void getAllGuests_ShouldReturnAllGuests() {
        Guest guest2 = new Guest(UUID.randomUUID(), "Jane Smith",
                "jane" + UUID.randomUUID() + "@example.com", null);

        guestService.addGuest(testGuest);
        guestService.addGuest(guest2);

        List<Guest> result = guestService.getAllGuests();
        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(g -> g.id().equals(testGuestId)));
        assertTrue(result.stream().anyMatch(g -> g.fullName().equals("Jane Smith")));
    }

    @Test
    void deleteGuest_ShouldDeleteGuest() {
        guestService.addGuest(testGuest);
        guestService.deleteGuest(testGuestId);

        Optional<Guest> result = guestService.getGuestById(testGuestId);
        assertFalse(result.isPresent());

        assertEquals(0, guestService.getAllGuests().size());
    }

    @Test
    void getTotalGuests_ShouldReturnCorrectCount() {
        guestService.addGuest(testGuest);
        guestService.addGuest(new Guest(UUID.randomUUID(), "Jane Smith",
                "jane" + UUID.randomUUID() + "@example.com", null));
        guestService.addGuest(new Guest(UUID.randomUUID(), "Bob Wilson",
                "bob" + UUID.randomUUID() + "@example.com", null));

        int total = guestService.getTotalGuests();
        assertEquals(3, total);
    }

    @Test
    void getTotalGuests_WhenNoGuests_ShouldReturnZero() {
        int total = guestService.getTotalGuests();
        assertEquals(0, total);
    }
}
