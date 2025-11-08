package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUserIdAndStatus(Long userId, Booking.BookingStatus status);

    List<Booking> findAllByStatusAndEndTimeBefore(Booking.BookingStatus bookingStatus, LocalDateTime now);

    void deleteByEndTimeBefore(LocalDateTime cutoff);
}
