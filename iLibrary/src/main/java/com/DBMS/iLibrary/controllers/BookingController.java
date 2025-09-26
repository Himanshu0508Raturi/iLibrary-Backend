package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.*;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.repository.SeatRepo;
import com.DBMS.iLibrary.service.BookingService;
import com.DBMS.iLibrary.service.SeatService;
import com.DBMS.iLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private SeatService seatService;
    @Autowired
    private SeatRepo seatRepo;

    @PostMapping("/seat")
    public ResponseEntity<?> bookASeat(@RequestBody SeatDTO seatdto)
    {
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(opUser.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        Seat seat = seatService.findBySeatNumber(seatdto.getSeatNumber());
        if(seat.getStatus() != Seat.SeatStatus.AVAILABLE)
        {
            return new ResponseEntity<>("Seat Not Available",HttpStatus.CONFLICT);
        }
        if(seat.getStatus() == (Seat.SeatStatus.UNDER_MAINTENANCE))
        {
            return new ResponseEntity<>("Seat Under Maintenance",HttpStatus.NO_CONTENT);
        }
        seat.setStatus(Seat.SeatStatus.BOOKED);
        seatRepo.save(seat);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSeat(seat);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStartTime(LocalDateTime.now());
        booking.setEndTime(LocalDateTime.now().plusHours(seatdto.getHours()));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setCreatedOffline(false);
        bookingRepo.save(booking);
        return new ResponseEntity<>("Seat No " + seatdto.getSeatNumber() + " booked Successfully",HttpStatus.OK);
    }
    @DeleteMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancelABooking(@PathVariable Long bookingId)
    {
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(opUser.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        Optional<Booking> opBooking = bookingRepo.findById(bookingId);
        if (opBooking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }
        Booking cancelBooking = opBooking.get();
        if(!cancelBooking.getUser().equals(user))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Booking does not belong to user");
        }
        cancelBooking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepo.save(cancelBooking);

        Seat seat = cancelBooking.getSeat();
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepo.save(seat);
        return ResponseEntity.ok("Booking canceled Successfully");
    }
}
