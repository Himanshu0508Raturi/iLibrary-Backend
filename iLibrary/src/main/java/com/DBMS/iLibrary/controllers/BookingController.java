package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.*;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.repository.SeatRepo;
import com.DBMS.iLibrary.service.BookingService;
import com.DBMS.iLibrary.service.MailService;
import com.DBMS.iLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private UserService userService;
    @Value("${app.jwtSecret}")
    private String secretKey;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private MailService mailService;
    @Autowired
    private BookingRepo bookingRepo;

    @PostMapping("/seat")
    public ResponseEntity<?> bookASeat(@RequestBody SeatDTO seatdto) {
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (opUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        try {
            bookingService.bookSeat(seatdto, user, secretKey);
            return new ResponseEntity<>("Seat No " + seatdto.getSeatNumber() + " booked Successfully", HttpStatus.OK);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Runtime Exception occurred in booking controller");
        }
    }

    @DeleteMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancelABooking(@PathVariable Long bookingId) {
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (opUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }
        Booking booking = bookingOpt.get();
        try {
            bookingService.cancelBooking(bookingId, user);
            mailService.sendCancellationMail(user, booking);
            return ResponseEntity.ok("Booking canceled Successfully");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Runtime Exception occurred in cancel controller");
        }
    }
}
