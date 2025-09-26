package com.DBMS.iLibrary.entity;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
public class SeatDTO {
    private String seatNumber;
    private int hours;
}
