package com.hotel.management.system.service;

import com.hotel.management.system.model.Payment;
import com.hotel.management.system.model.PaymentMethod;
import com.hotel.management.system.model.PaymentStatus;
import com.hotel.management.system.model.Reservation;
import com.hotel.management.system.repository.IPaymentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaymentService {
    private final IPaymentRepository paymentRepository;

    public PaymentService(IPaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment makePayment(Reservation reservation, double amount, PaymentMethod paymentMethod) {
        Payment payment = new Payment(UUID.randomUUID(), reservation, LocalDateTime.now(), amount, PaymentStatus.PAID ,paymentMethod);
        paymentRepository.save(payment);
        return payment;
    }

    public Payment makePayment(Reservation reservation, double amount, PaymentMethod paymentMethod, String receiver) {
        Payment payment = new Payment(UUID.randomUUID(), reservation, LocalDateTime.now(), amount, PaymentStatus.PAID ,paymentMethod, receiver);
        paymentRepository.save(payment);
        return payment;
    }

    public Optional<Payment> getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public double getTodayRevenue(LocalDate today) {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getPaymentDate().toLocalDate().isEqual(today))
                .mapToDouble(Payment::getAmount)
                .sum();
    }
}
