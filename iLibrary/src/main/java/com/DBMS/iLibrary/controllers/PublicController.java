package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.service.MailService;
import com.DBMS.iLibrary.service.UserService;
import com.DBMS.iLibrary.utilities.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MailService mailService;

    // Health check function to check api is successfully deployed in server.
    @GetMapping("/healthCheck")
    public ResponseEntity<?> getHealth() {
        return new ResponseEntity<>("Health : OK", HttpStatus.OK);
    }
    // Saves user detail in user table first time. add prefix role in user's role
    // Mail service involves.
    // Request Body -> User entity
    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        Set<String> roles = user.getRoles();

        // Add ROLE_ prefix if missing (Spring Security convention)
        Set<String> prefixedRoles = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toSet());
        try {
            userService.saveUser(user, prefixedRoles); // Save user first.
            mailService.sendSignupMail(user); // Then send otp mail
            return new ResponseEntity<>("User Registered Successfully", HttpStatus.CREATED);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Signup succeeded, but failed to send email.", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error while saving user.", HttpStatus.BAD_REQUEST);
        }
    }
    // add user to Spring Security Context Holder simply means loggin in user and send a token as a response
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            String token = jwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok().body(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
