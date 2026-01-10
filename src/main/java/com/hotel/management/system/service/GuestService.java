package com.hotel.management.system.service;

import com.hotel.management.system.model.Guest;
import com.hotel.management.system.repository.IGuestRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GuestService {
    private final IGuestRepository guestRepository;

    public GuestService(IGuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public void addGuest(Guest guest) {
        guestRepository.save(guest);
    }

    public Optional<Guest> getGuestById(UUID guestId) {
        return guestRepository.findById(guestId);
    }

    public List<Guest> getAllGuests() {
        return guestRepository.findAll();
    }

    public void updateGuest(Guest guest) {
        guestRepository.update(guest);
    }

    public void deleteGuest(UUID guestId) {
        guestRepository.deleteById(guestId);
    }

    public int getTotalGuests() {
        return guestRepository.findAll().size();
    }
}