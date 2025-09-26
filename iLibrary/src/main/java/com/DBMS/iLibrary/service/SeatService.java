package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Seat;
import com.DBMS.iLibrary.repository.SeatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.Query;

@Service
public class SeatService {
    @Autowired
    private SeatRepo seatRepo;

    public Seat findBySeatNumber(String seatNumber)
    {
        return seatRepo.findBySeatNumber(seatNumber);
    }

}
