package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepo extends JpaRepository<Payment, Long> {
}
