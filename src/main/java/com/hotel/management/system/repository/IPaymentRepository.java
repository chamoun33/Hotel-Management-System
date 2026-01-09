package com.hotel.management.system.repository;

import com.hotel.management.system.model.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(UUID id);
    List<Payment> findAll();
    void deleteById(UUID id);
}

