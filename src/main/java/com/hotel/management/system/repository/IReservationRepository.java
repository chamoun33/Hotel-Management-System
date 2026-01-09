package com.hotel.management.system.repository;

import com.hotel.management.system.model.Reservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IReservationRepository {
    void save(Reservation reservation);
    Optional<Reservation> findById(UUID id);
    List<Reservation> findAll();
    void deleteById(UUID id);
}
