package com.hotel.management.system.repository;

import com.hotel.management.system.model.Reservation;

import java.util.*;

public class ReservationRepository implements IReservationRepository {
    private final Map<UUID, Reservation> reservations = new HashMap<>();

    @Override
    public void save(Reservation reservation) {
        reservations.put(reservation.getId(), reservation);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations.values());
    }

    @Override
    public void deleteById(UUID id) {
        reservations.remove(id);
    }
}
