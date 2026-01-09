package com.hotel.management.system.repository;

import com.hotel.management.system.model.Guest;

import java.util.*;

public class GuestRepository implements IGuestRepository {
    private final Map<UUID, Guest> guests = new HashMap<>();

    @Override
    public void save(Guest guest) {
        guests.put(guest.id(), guest);
    }

    @Override
    public Optional<Guest> findById(UUID id) {
        return Optional.ofNullable(guests.get(id));
    }

    @Override
    public List<Guest> findAll() {
        return new ArrayList<>(guests.values());
    }

    @Override
    public void deleteById(UUID id) {
        guests.remove(id);
    }
}
