package com.DBMS.iLibrary.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private SubscriptionType type;

    public enum SubscriptionStatus {
        ACTIVE, PASSIVE
    }
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
