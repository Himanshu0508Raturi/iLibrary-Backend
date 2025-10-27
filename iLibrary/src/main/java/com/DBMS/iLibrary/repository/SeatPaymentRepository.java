package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.SeatPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatPaymentRepository extends JpaRepository<SeatPayment,Long> {
    Optional<SeatPayment> findBySessionId(String sessionId);
    List<SeatPayment> findAllByUserId(Long userId);
    //Optional<SeatPayment> findByUserId(Long id);
}
