package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.BookingRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.DBMS.iLibrary.entity.Booking.BookingStatus.*;

@Service
public class LibrarianService {
    @Value("${app.jwtSecret}")
    private String secretKey;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private MailService mailService;


    public String verifyQrToken(String qrToken) {
        Jws<Claims> claimsJws = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(qrToken);

        Claims claims = claimsJws.getBody();

        Long bookingId = Long.valueOf(claims.get("bookingId").toString());
        String seatNumber = claims.get("seatNumber").toString();
        String status = claims.get("status").toString();
        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found");
        }
        Booking booking = bookingOpt.get();
        if (booking.getIsPaymentDone() == 0) {
            return "Payment has not been done yet";
        }
        if (!booking.getStatus().toString().equals(status)) {
            throw new IllegalStateException("Booking status mismatch");
        }
        LocalDateTime endTime = LocalDateTime.parse(claims.get("endTime").toString());
        if (LocalDateTime.now().isAfter(endTime)) {
            throw new IllegalStateException("Booking expired");
        }
        int bookedHours = booking.getHrs();  // original duration requested
        LocalDateTime now = LocalDateTime.now();
        if (booking.getStatus().equals(CANCELLED))
            return "Payment has been cancelled.";
        if (booking.getStatus().equals(FAILED))
            return "Payment has been failed.";
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setStartTime(now);

        booking.setEndTime(now.plusHours(bookedHours));
        User user = booking.getUser();
        bookingRepo.save(booking);
        try {
            mailService.sendConformationMail(user, booking);
        } catch (MessagingException e) {
            System.out.print("Error at librarian service conform message function.");
            throw new RuntimeException(e);
        }
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart(); // Java 9+
        return "Booking verified successfully for seat number: " + seatNumber +
                ", Duration: " + hours + " hours " + minutes + " minutes";
    }
}
