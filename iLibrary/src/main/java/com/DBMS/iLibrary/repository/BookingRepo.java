package com.DBMS.iLibrary.repository;

import com.DBMS.iLibrary.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepo extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUserIdAndStatus(Long userId, Booking.BookingStatus status);
    List<Booking> findAllByStatusAndEndTimeBefore(Booking.BookingStatus bookingStatus, LocalDateTime now);
    void deleteByEndTimeBefore(LocalDateTime cutoff);
    void deleteByUserId(Long id);
    List<Booking> findAllByUserId(Long id);

}
