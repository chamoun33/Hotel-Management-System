package com.hotel.management.system.repository;

import com.hotel.management.system.model.Payment;

import java.util.*;

public class PaymentRepository implements IPaymentRepository {
    private final Map<UUID, Payment> payments = new HashMap<>();

    @Override
    public void save(Payment payment) {
        payments.put(payment.getId(), payment);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return Optional.ofNullable(payments.get(id));
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(payments.values());
    }

    @Override
    public void deleteById(UUID id) {
        payments.remove(id);
    }
}
