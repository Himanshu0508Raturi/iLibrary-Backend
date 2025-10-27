package com.DBMS.iLibrary.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // A booking is made by one user, not multiple users.
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    private LocalDateTime bookingDate;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    public enum BookingStatus {
        PENDING,     // Booking has been requested but not yet confirmed
        CONFIRMED,   // Booking is approved and valid
        CANCELLED,   // Booking was canceled by user or admin
        FAILED       // Booking failed (e.g., payment failed, seat unavailable)
    }

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private int hrs;
    private double amount;
    private boolean paymentDone;
    public boolean getPaymentDone() {
        return paymentDone;
    }
}
