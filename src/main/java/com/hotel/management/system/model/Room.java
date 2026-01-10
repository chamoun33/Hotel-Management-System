package com.hotel.management.system.model;

import java.util.Objects;

public class Room {
    private final int roomNumber;
    private final int capacity;
    private RoomType roomType;
    private double pricePerNight;
    private RoomStatus status;

    public Room(int roomNumber, int capacity, RoomType roomType, double pricePerNight, RoomStatus status) {
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.status = status;
    }

    public int getRoomNumber() { return roomNumber; }
    public int getCapacity() { return capacity; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType type) { this.roomType = type; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public boolean isAvailable() { return status == RoomStatus.AVAILABLE; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room other)) return false;
        return roomNumber == other.roomNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }
}
