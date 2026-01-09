package com.hotel.management.system.repository;

import com.hotel.management.system.model.Room;

import java.util.*;

public class RoomRepository implements IRoomRepository {
    private final Map<Integer, Room> rooms = new HashMap<>();

    @Override
    public void save(Room room) {
        rooms.put(room.getRoomNumber(), room);
    }

    @Override
    public Optional<Room> findByNumber(int roomNumber) {
        return Optional.ofNullable(rooms.get(roomNumber));
    }

    @Override
    public List<Room> findAll() {
        return new ArrayList<>(rooms.values());
    }

    @Override
    public void deleteByNumber(int roomNumber) {
        rooms.remove(roomNumber);
    }
}
