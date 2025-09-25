package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepo extends JpaRepository<Seat, Long> {
}
