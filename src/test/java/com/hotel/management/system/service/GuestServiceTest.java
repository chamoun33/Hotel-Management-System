package com.hotel.management.system.service;

import com.hotel.management.system.model.Guest;
import com.hotel.management.system.repository.GuestRepository;
import com.hotel.management.system.repository.IGuestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GuestServiceTest {

    private GuestService guestService;
    private Guest testGuest;
    private UUID testGuestId;

    @BeforeEach
    void setUp() {
        IGuestRepository guestRepository = new GuestRepository();
        guestService = new GuestService(guestRepository);

        testGuestId = UUID.randomUUID();
        testGuest = new Guest(testGuestId, "John Doe", "john@example.com", null);
    }

    @Test
    void addGuest_ShouldSaveGuest() {
        guestService.addGuest(testGuest);

        List<Guest> allGuests = guestService.getAllGuests();
        assertEquals(1, allGuests.size());
        assertEquals(testGuest, allGuests.getFirst());
    }

    @Test
    void getGuestById_WhenGuestExists_ShouldReturnGuest() {
        guestService.addGuest(testGuest);

        Optional<Guest> result = guestService.getGuestById(testGuestId);
        assertTrue(result.isPresent());
        assertEquals(testGuest, result.get());
    }

    @Test
    void getGuestById_WhenGuestDoesNotExist_ShouldReturnEmpty() {
        Optional<Guest> result = guestService.getGuestById(testGuestId);
        assertFalse(result.isPresent());
    }

    @Test
    void getAllGuests_ShouldReturnAllGuests() {
        Guest guest2 = new Guest(UUID.randomUUID(), "Jane Smith", "jane@example.com", null);
        guestService.addGuest(testGuest);
        guestService.addGuest(guest2);

        List<Guest> result = guestService.getAllGuests();
        assertEquals(2, result.size());
        assertTrue(result.contains(testGuest));
        assertTrue(result.contains(guest2));
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
        guestService.addGuest(new Guest(UUID.randomUUID(), "Jane Smith", "jane@example.com", null));
        guestService.addGuest(new Guest(UUID.randomUUID(), "Bob Wilson", "bob@example.com", null));

        int total = guestService.getTotalGuests();
        assertEquals(3, total);
    }

    @Test
    void getTotalGuests_WhenNoGuests_ShouldReturnZero() {
        int total = guestService.getTotalGuests();
        assertEquals(0, total);
    }
}
