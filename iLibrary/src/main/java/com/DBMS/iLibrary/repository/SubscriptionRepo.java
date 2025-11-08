package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserId(Long Id);
    Optional<Subscription> findByUserAndStatus(User user, Subscription.SubscriptionStatus status);
    List<Subscription> findAllByUserAndStatus(User user, Subscription.SubscriptionStatus status);
    List<Subscription> findByEndDateBeforeAndStatus(LocalDateTime now, Subscription.SubscriptionStatus active);
    void deleteByEndDateBefore(LocalDate cutoff);

    List<Subscription> findAllByUser(User user);
}
