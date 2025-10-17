package com.DBMS.iLibrary.entity;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long amount;
    private Long quantity;
    private String name;
    private String currency;
}
