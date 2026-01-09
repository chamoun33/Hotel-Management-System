package com.hotel.management.system.model;

import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.util.UUID;

public record Guest(
        UUID id,
        String fullName,
        String email,
        PhoneNumber phoneNumber
) {}
