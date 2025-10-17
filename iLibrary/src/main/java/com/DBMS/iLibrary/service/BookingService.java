package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.Seat;
import com.DBMS.iLibrary.entity.SeatDTO;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.repository.SeatRepo;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private SeatService seatService;
    @Autowired
    private SeatRepo seatRepo;
    @Autowired
    private QrcodeService qrcodeService;

    @Transactional
    public void bookSeat(SeatDTO seatdto, User user, String secretKey) {
        LocalDateTime now = LocalDateTime.now();
        Seat seat = seatService.findBySeatNumber(seatdto.getSeatNumber());
        if (seat == null) {
            throw new IllegalArgumentException("Seat not found. Enter valid seat number.");
        }
        if (seat.getStatus() == Seat.SeatStatus.UNDER_MAINTENANCE) {
            throw new IllegalStateException("Seat under maintenance,not available for booking");
        }
        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Seat not available. Already Booked.");
        }

        seat.setStatus(Seat.SeatStatus.BOOKED);
        seatRepo.save(seat);

        Booking booking = new Booking();
        booking.setHrs(seatdto.getHours());
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setBookingDate(now);
        booking.setStartTime(now);
        booking.setEndTime(now.plusHours(seatdto.getHours()));
        booking.setStatus(Booking.BookingStatus.PENDING);
        bookingRepo.save(booking);

        // Generate JWT QR data
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Map<String, Object> payload = new HashMap<>();
        payload.put("bookingId", booking.getId());
        payload.put("userId", booking.getUser().getId());
        payload.put("seatId", booking.getSeat().getId());
        payload.put("seatNumber", booking.getSeat().getSeatNumber());
        payload.put("endTime", booking.getEndTime().toString());
        payload.put("status", booking.getStatus().toString());
        String data = Jwts.builder()
                .setSubject("BookingQR")
                .addClaims(payload)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        try {
            qrcodeService.createQrCode(data, user.getEmail(),booking);
        } catch (Exception e) {
            // Throw a runtime exception to trigger rollback
            throw new RuntimeException("Failed to generate/send QR code email", e);
        }
    }
    @Transactional
    public void cancelBooking(Long bookingId, User user) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (!booking.getUser().equals(user)) {
            throw new IllegalStateException("Booking does not belong to user");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepo.save(booking);

        Seat seat = booking.getSeat();
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepo.save(seat);
    }
}
