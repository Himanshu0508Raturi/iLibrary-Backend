package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.service.UserService;
import com.DBMS.iLibrary.utilities.JwtUtil;
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

    @GetMapping("/healthCheck")
    public ResponseEntity<?> getHealth()
    {
        return new ResponseEntity<>("Health : OK" , HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        boolean res = false;
        Set<String> roles = user.getRoles();

        // Optional: Add ROLE_ prefix if missing (assuming your roles are like STUDENT, ADMIN, etc.)
        Set<String> prefixedRoles = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.toSet());

        res = userService.saveUser(user, prefixedRoles);

        if (res) {
            return new ResponseEntity<>("User Registered.", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("Error While Saving User.", HttpStatus.BAD_REQUEST);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user)
    {
        try
        {
            Authentication authentication = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
            String token = jwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok().body(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }
}
