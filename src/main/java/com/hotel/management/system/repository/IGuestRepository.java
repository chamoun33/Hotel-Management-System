package com.hotel.management.system.repository;

import com.hotel.management.system.model.Guest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IGuestRepository {
    void save(Guest guest);
    Optional<Guest> findById(UUID id);
    List<Guest> findAll();
    void deleteById(UUID id);
    void update(Guest guest);
}
