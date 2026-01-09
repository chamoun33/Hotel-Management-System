package com.hotel.management.system.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Reservation {
    private final UUID id;
    private final Guest guest;
    private final Room room;
    private final LocalDateTime created_at = LocalDateTime.now();
    private LocalDate checkIn;
    private LocalDate checkOut;
    private ReservationStatus status;

    public Reservation(UUID id, Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, ReservationStatus status) {
        this.id = id;
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
    }

    public UUID getId() { return id; }
    public Guest getGuest() { return guest; }
    public Room getRoom() { return room; }
    public LocalDateTime getCreated_at() { return created_at; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
}
