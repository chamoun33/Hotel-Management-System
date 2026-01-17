package com.hotel.management.system.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Payment {
    private final UUID id;
    private final Reservation reservation;
    private LocalDateTime paymentDate;
    private double amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String receiver;

    public Payment(UUID id, Reservation reservation, LocalDateTime paymentDate, double amount, PaymentStatus status, PaymentMethod method) {
        this.id = id;
        this.reservation = reservation;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.status = status;
        this.method = method;
    }
    public Payment(UUID id, Reservation reservation, LocalDateTime paymentDate, double amount, PaymentStatus status, PaymentMethod method, String receiver) {
        this.id = id;
        this.reservation = reservation;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.receiver = receiver;
    }

    public UUID getId() { return id; }
    public Reservation getReservation() { return reservation; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public void setReceiver(String receiver){this.receiver = receiver; }
    public String getReceiver() {return receiver; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}