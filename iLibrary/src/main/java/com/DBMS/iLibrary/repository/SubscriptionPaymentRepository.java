package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.SeatPayment;
import com.DBMS.iLibrary.entity.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment,Long> {
    Optional<SubscriptionPayment> findBySessionId(String sessionId);
    List<SubscriptionPayment> findAllByUserId(Long userId);

    void deleteByUserId(Long id);
}
