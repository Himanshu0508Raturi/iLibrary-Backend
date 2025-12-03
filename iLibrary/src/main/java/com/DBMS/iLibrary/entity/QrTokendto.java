package com.DBMS.iLibrary.entity;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class QrTokendto {
    @NotEmpty(message = "Qr token shouldn't be empty.!!")
    private String qrToken;
}
