package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUserIdAndStatus(Long userId, Booking.BookingStatus status);
}
