package com.DBMS.iLibrary.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)// Auto assign id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public enum SubscriptionType {
        WEEKLY, MONTHLY, YEARLY
    }

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Subscription type shouldn't be null")
    private SubscriptionType type;

    public enum SubscriptionStatus {
        ACTIVE, PASSIVE, EXPIRED
    }

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
    private BigDecimal amount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
