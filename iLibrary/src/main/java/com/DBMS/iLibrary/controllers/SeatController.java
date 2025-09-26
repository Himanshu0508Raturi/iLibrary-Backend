package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Seat;
import com.DBMS.iLibrary.repository.SeatRepo;
import com.DBMS.iLibrary.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seat")
public class SeatController {
    @Autowired
    private SeatService seatService;
    @Autowired
    private SeatRepo seatRepo;

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableSeat()
    {
        List<Seat> available = seatRepo.findByStatus(Seat.SeatStatus.AVAILABLE);
        return new ResponseEntity<>(available , HttpStatus.OK);
    }
}
