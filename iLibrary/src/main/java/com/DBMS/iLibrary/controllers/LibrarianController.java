package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.QrTokendto;
import com.DBMS.iLibrary.service.LibrarianService;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/librarian")
public class LibrarianController {

    @Autowired
    private LibrarianService librarianService;

    // Verify qr received by the user through mail.
    // How this method verify : read jwt token from user's QR , convert token to info(bookingId,seatNumber,status,endTimeStr)
    // using jws Claims and secret key, set booking status to CONFORMED in booking table , change start and end time of booking
    // in booking table accordingly and then save to booking table.
    // uses Mail service
    @PostMapping("/verify-qr")
    public ResponseEntity<?> verifyQrCode(@Valid @RequestBody QrTokendto token) {
        try {
            String response = librarianService.verifyQrToken(token.getQrToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or tampered QR token");
        }
    }
}

