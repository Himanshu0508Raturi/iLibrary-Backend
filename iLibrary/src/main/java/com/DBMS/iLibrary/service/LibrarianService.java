package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.QrTokendto;
import com.DBMS.iLibrary.repository.BookingRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LibrarianService {
    @Value("${app.jwtSecret}")
    private String secretKey;
    @Autowired
    private BookingRepo bookingRepo;


    public String verifyQrToken(String qrToken) {
        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(qrToken);

        Claims claims = claimsJws.getBody();

        Long bookingId = Long.valueOf(claims.get("bookingId").toString());
        String seatNumber = claims.get("seatNumber").toString();
        String status = claims.get("status").toString();
        String endTimeStr = claims.get("endTime").toString();

        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found");
        }
        Booking booking = bookingOpt.get();

        if (!booking.getStatus().toString().equals(status)) {
            throw new IllegalStateException("Booking status mismatch");
        }
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        if (LocalDateTime.now().isAfter(endTime)) {
            throw new IllegalStateException("Booking expired");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setStartTime(LocalDateTime.now());
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart(); // Java 9+
        booking.setEndTime(LocalDateTime.now().plusHours(hours));
        bookingRepo.save(booking);

        return "Booking verified successfully for seat number: " + seatNumber +
                ", Duration: " + hours + " hours " + minutes + " minutes";
    }
}
