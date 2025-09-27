package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.QrTokendto;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.service.LibrarianService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/librarian")
public class LibrarianController {

    @Autowired
    private LibrarianService librarianService;

    @PostMapping("/verify-qr")
    public ResponseEntity<?> verifyQrCode(@RequestBody QrTokendto token) {
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

