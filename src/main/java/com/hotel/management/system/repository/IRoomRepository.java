package com.hotel.management.system.repository;

import com.hotel.management.system.model.Room;
import com.hotel.management.system.model.RoomStatus;

import java.util.List;
import java.util.Optional;

public interface IRoomRepository {
    void save(Room room);
    Optional<Room> findByNumber(int roomNumber);
    List<Room> findAll();
    void deleteByNumber(int roomNumber);
    void updateStatus(int roomNumber, RoomStatus status);
}
