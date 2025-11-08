package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SeatRepo extends JpaRepository<Seat, Long> {
    Seat findBySeatNumber(String seatNumber);
    List<Seat> findByStatus(Seat.SeatStatus status);
}
