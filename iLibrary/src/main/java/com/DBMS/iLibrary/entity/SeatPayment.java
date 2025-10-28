package com.DBMS.iLibrary.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Seat_Payment")
public class SeatPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne /*Each SeatPayment record is linked to exactly one user (the one who made the payment).But one user can have many seat payments in total.*/
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne /*Each SeatPayment is linked to one specific seat.But a seat can be associated with multiple payments (if it’s booked and released multiple times).*/
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    private BigDecimal amount;
    private LocalDateTime transactionDate = LocalDateTime.now();
    private String sessionId; // used in saveDataAfterPayment for finding payment.
    @Enumerated(EnumType.STRING)
    private SeatPayment.PaymentStatus status = PaymentStatus.PENDING;

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}
