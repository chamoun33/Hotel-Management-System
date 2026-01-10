package com.hotel.management.system.service;

import com.hotel.management.system.exception.HotelManagementException;
import com.hotel.management.system.model.*;
import com.hotel.management.system.repository.IGuestRepository;
import com.hotel.management.system.repository.IReservationRepository;
import com.hotel.management.system.repository.IRoomRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ReservationService {
    private final IReservationRepository reservationRepository;
    private final IRoomRepository roomRepository;
    private final IGuestRepository guestRepository;
    private final RoomService roomService;

    public ReservationService(IReservationRepository reservationRepository,
                              IRoomRepository roomRepository,
                              IGuestRepository guestRepository,
                              RoomService roomService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.roomService = roomService;
    }

    public Reservation createReservation(UUID guestId, int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        if (!roomService.isRoomAvailable(roomNumber, checkIn, checkOut)) {
            throw new HotelManagementException("Room not available for selected dates");
        }

        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new HotelManagementException("Guest not found"));

        Room room = roomRepository.findByNumber(roomNumber)
                .orElseThrow(() -> new HotelManagementException("Room not found"));

        Reservation reservation = new Reservation(UUID.randomUUID(), guest, room, checkIn, checkOut, ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
        return reservation;
    }

    public void cancelReservation(UUID reservationId) {
        reservationRepository.updateStatus(reservationId, ReservationStatus.CANCELLED);
    }

    public List<Reservation> getReservationsByGuest(UUID guestId) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getGuest().id().equals(guestId))
                .collect(Collectors.toList());
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public long getOccupiedRooms(LocalDate today) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .filter(r -> !r.getCheckIn().isAfter(today) && r.getCheckOut().isAfter(today))
                .count();
    }

    public void checkIn(UUID reservationId) {
        // Load reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new HotelManagementException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new HotelManagementException("Only confirmed reservations can check in");
        }

        // Load full room details
        Room fullRoom = roomRepository.findByNumber(reservation.getRoom().getRoomNumber())
                .orElseThrow(() -> new HotelManagementException("Room not found"));

        // Update reservation status
        reservationRepository.updateStatus(reservationId, ReservationStatus.CHECKED_IN);

        // Update room status
        roomRepository.updateStatus(fullRoom.getRoomNumber(), RoomStatus.OCCUPIED);
    }

    public void checkOut(UUID reservationId) {
        // Load reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new HotelManagementException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new HotelManagementException("Only checked-in reservations can check out");
        }

        reservationRepository.updateStatus(reservationId, ReservationStatus.CHECKED_OUT);

        roomRepository.updateStatus(reservation.getRoom().getRoomNumber(), RoomStatus.AVAILABLE);
    }


    public long getCheckInsToday(LocalDate today) {
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getCheckIn().equals(today) && reservation.getStatus() == ReservationStatus.CONFIRMED)
                .count();
    }

    public long getCheckOutsToday(LocalDate today) {
        return reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getCheckOut().equals(today) && reservation.getStatus() == ReservationStatus.CHECKED_IN)
                .count();
    }

    public long getNumberOfNights(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new HotelManagementException("Reservation not found"));

        return ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
    }

}
