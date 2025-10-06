package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.Seat;
import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.repository.SeatRepo;
import com.DBMS.iLibrary.repository.SubscriptionRepo;
import com.DBMS.iLibrary.repository.UserRepository;
import com.DBMS.iLibrary.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private SubscriptionRepo subscriptionRepo;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private SeatRepo seatRepo;
    @Autowired
    private UserRepository userRepository;

    // Return all subscription from subscription table.
    @GetMapping("/allSubscription")
    public ResponseEntity<?> getAllSubs() {
        List<Subscription> allSubs = subscriptionRepo.findAll();
        if (!allSubs.isEmpty()) {
            return new ResponseEntity<>(allSubs, HttpStatus.OK);
        }
        return new ResponseEntity<>("No Active Subscription for any user.", HttpStatus.NO_CONTENT);
    }
    // Return all Bookings from booking table.
    @GetMapping("/allBooking")
    public ResponseEntity<?> getAllBooking() {
        List<Booking> allBooking = bookingRepo.findAll();
        if (!allBooking.isEmpty()) {
            return new ResponseEntity<>(allBooking, HttpStatus.OK);
        }
        return new ResponseEntity<>("No Active Booking for any user.", HttpStatus.NO_CONTENT);
    }
    // Return all Seats from seats table.
    @GetMapping("/allSeats")
    public ResponseEntity<?> getAllSeats() {
        List<Seat> allSeat = seatRepo.findAll();
        if (!allSeat.isEmpty()) {
            return new ResponseEntity<>(allSeat, HttpStatus.OK);
        }
        return new ResponseEntity<>("No Seats in DB.", HttpStatus.NO_CONTENT);
    }
    // Return all Users from User table.
    @GetMapping("/allUsers")
    public ResponseEntity<?> getAllUsers() {
        List<User> all = userRepository.findAll();
        if (!all.isEmpty()) {
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>("No Users in DB.", HttpStatus.NO_CONTENT);
    }
}
