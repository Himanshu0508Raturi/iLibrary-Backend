package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/changeDetail")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/bookingHistory")
    public ResponseEntity<?> getUserBookingHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> OpUser = userService.findByUsername(username);
        if (OpUser.isEmpty()) {
            return new ResponseEntity<>("User with username : " + username + " not found.", HttpStatus.NO_CONTENT);
        }
        List<Booking> all = userService.getUserBookingHistory(OpUser.get());
        return new ResponseEntity<>(all, HttpStatus.OK);
    }

    //subscription of logged - in user
    @GetMapping("/activeSubscription")
    public ResponseEntity<?> getUserSubscription() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).get();
        Subscription subscription = new Subscription();
        try {
            subscription = userService.getActiveSubscriptionOfUser(user);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        return new ResponseEntity<>(subscription, HttpStatus.OK);
    }

    // get all subscription of a user.
    @GetMapping("/allSubscription")
    public ResponseEntity<?> getAllUserSubscription() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).get();
        List<Subscription> allSubscription = null;
        try {
            allSubscription = userService.getAllSubscriptionOfUser(user);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
        return new ResponseEntity<>(allSubscription, HttpStatus.OK);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<?> deleteAUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username).get();
        try {
            userService.deleteAUser(user);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>("User with username: " + username + " deleted Successfully", HttpStatus.OK);
    }
}
