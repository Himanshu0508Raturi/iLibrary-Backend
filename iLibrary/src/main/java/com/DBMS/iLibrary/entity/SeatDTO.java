package com.DBMS.iLibrary.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SeatDTO {
    @NotEmpty(message = "SeatNumber should not be empty")
    private String seatNumber;
    @Min(value = 1 ,message = "Hours should be greater than 0.")
    private int hours;
}
