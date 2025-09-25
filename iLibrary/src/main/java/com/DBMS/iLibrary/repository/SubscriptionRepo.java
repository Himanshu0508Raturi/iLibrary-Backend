package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserId(Long Id);
}
