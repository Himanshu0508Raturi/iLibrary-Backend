package com.DBMS.iLibrary.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)// Auto assign id
    private Long id;
    @Column(unique = true)
    private String seatNumber; // L-1 , L-250
    private String location; // 1st Floor , 3rd Floor

    public enum SeatStatus {
        AVAILABLE,
        BOOKED,
        UNDER_MAINTENANCE
    }
    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE; // Enum for seat status
}
