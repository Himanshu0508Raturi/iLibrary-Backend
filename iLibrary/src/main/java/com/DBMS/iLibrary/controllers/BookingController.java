package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.*;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.service.BookingService;
import com.DBMS.iLibrary.service.MailService;
import com.DBMS.iLibrary.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/booking")
@CrossOrigin
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

    // Books a seat for a logged-in user in booking table : Request Body : seatdto.
    // Transactional bookSeat() method().
    // Mail service Involved.
//    @CachePut(key = "abc")
    @PostMapping("/seat")
    public ResponseEntity<?> bookASeat(@Valid @RequestBody SeatDTO seatdto) {
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

    // cancel a seat for a login in user in booking table .Table data wouldn't be deleted only status set to CANCEL in booking
    // table for history purpose , also seat status is set to AVAILABLE in seat table.
    // mail service involved
    //Transactional cancelBooking() method
//    @CacheEvict()
    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelABooking() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> opUser = userService.findByUsername(username);

        if (opUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = opUser.get();

        // Fetch all bookings for this user with status CONFIRMED or PENDING
        List<Booking> userBookings = new ArrayList<>();
        userBookings.addAll(bookingRepo.findAllByUserIdAndStatus(user.getId(), Booking.BookingStatus.CONFIRMED));
        userBookings.addAll(bookingRepo.findAllByUserIdAndStatus(user.getId(), Booking.BookingStatus.PENDING));

        if (userBookings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active (CONFIRMED or PENDING) bookings found to cancel.");
        }

        // Get the most recent booking (last one)
        Booking bookingToCancel = userBookings.get(userBookings.size() - 1);
        Long bookingId = bookingToCancel.getId();

        try {
            bookingService.cancelBooking(bookingId, user);
            mailService.sendCancellationMail(user, bookingToCancel);
            return ResponseEntity.ok("Booking cancelled successfully.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while cancelling booking.");
        }
    }

}

