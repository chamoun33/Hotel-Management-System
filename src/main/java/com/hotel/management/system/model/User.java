package com.hotel.management.system.model;

import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID id;
    private final String username;
    private final LocalDateTime created_at = LocalDateTime.now();
    private String password;
    private Role role;
    private PhoneNumber phoneNumber;

    public User(UUID id, String username, String password, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public LocalDateTime getCreated_at() { return created_at; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public void setPassword(String newPassword) { this.password = newPassword; }
    public void setRole(Role newRole) { this.role = newRole; }
    public PhoneNumber getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(PhoneNumber phoneNumber) { this.phoneNumber = phoneNumber; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
