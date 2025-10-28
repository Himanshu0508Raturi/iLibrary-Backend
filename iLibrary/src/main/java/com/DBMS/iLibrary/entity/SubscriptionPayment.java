package com.DBMS.iLibrary.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "subscription_Payment")
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    private BigDecimal amount;

    private LocalDateTime transactionDate = LocalDateTime.now();

    private String sessionId; // used in saveDataAfterPayment for finding payment.

    @Enumerated(EnumType.STRING)
    private SubscriptionPayment.subscriptionPaymentStatus status = SubscriptionPayment.subscriptionPaymentStatus.PENDING;

    public enum subscriptionPaymentStatus {
        PENDING,
        COMPLETED,
        FAILED
    }
}
