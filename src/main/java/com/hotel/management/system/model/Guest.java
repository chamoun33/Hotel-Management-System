package com.hotel.management.system.model;

import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.util.Objects;
import java.util.UUID;

public record Guest(
        UUID id,
        String fullName,
        String email,
        PhoneNumber phoneNumber
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
