package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserId(Long Id);
    Optional<Subscription> findByUserAndStatus(User user, Subscription.SubscriptionStatus status);

}
