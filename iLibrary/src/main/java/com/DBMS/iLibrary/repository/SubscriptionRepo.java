package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
}
