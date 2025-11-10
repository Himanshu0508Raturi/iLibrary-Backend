package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Seat;
import com.DBMS.iLibrary.repository.SeatRepo;
import com.DBMS.iLibrary.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/seat")
@CrossOrigin(origins = "http://localhost:8081", allowedHeaders = "*", allowCredentials = "true")
public class SeatController {
    @Autowired
    private SeatService seatService;
    @Autowired
    private SeatRepo seatRepo;
    // Send list of all available seats in seat table.
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSeat() {
        // Fetch available seats
        List<Seat> availableSeats = seatRepo.findByStatus(Seat.SeatStatus.AVAILABLE);

        // Extract only the seat names/numbers
        List<String> seatNames = availableSeats.stream()
                .map(Seat::getSeatNumber)  // or .map(Seat::getName) depending on your entity field
                .collect(Collectors.toList());

        return new ResponseEntity<>(seatNames, HttpStatus.OK);
    }

}
