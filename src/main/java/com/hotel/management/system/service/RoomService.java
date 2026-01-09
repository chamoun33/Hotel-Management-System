package com.hotel.management.system.service;

import com.hotel.management.system.model.ReservationStatus;
import com.hotel.management.system.model.Room;
import com.hotel.management.system.model.RoomStatus;
import com.hotel.management.system.repository.IReservationRepository;
import com.hotel.management.system.repository.IRoomRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomService {
    private final IRoomRepository roomRepository;
    private final IReservationRepository reservationRepository;

    public RoomService(IRoomRepository roomRepository, IReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public void addRoom(Room room) {
        roomRepository.save(room);
    }

    public Optional<Room> getRoom(int roomNumber) {
        return roomRepository.findByNumber(roomNumber);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public void deleteRoom(int roomNumber) {
        roomRepository.deleteByNumber(roomNumber);
    }

    public boolean isRoomAvailable(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        Optional<Room> room = roomRepository.findByNumber(roomNumber);
        if (room.isEmpty() || room.get().getStatus() != RoomStatus.AVAILABLE) {
            return false;
        }
        return reservationRepository.findAll().stream()
                .filter(r -> r.getRoom().getRoomNumber() == roomNumber)
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .noneMatch(r -> !r.getCheckOut().isBefore(checkIn) && !r.getCheckIn().isAfter(checkOut));
    }


    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomRepository.findAll().stream()
                .filter(room -> isRoomAvailable(room.getRoomNumber(), checkIn, checkOut))
                .collect(Collectors.toList());
    }

    public long getRoomsUnderMaintenance() {
        return roomRepository.findAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.MAINTENANCE)
                .count();
    }
}