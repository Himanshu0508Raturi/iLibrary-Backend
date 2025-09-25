package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.SubscriptionRepo;
import com.DBMS.iLibrary.service.SubscriptionService;
import com.DBMS.iLibrary.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/subscription")
public class SubscriptionControllers {
    @Autowired
    private SubscriptionService subsService;

    @Autowired
    private SubscriptionRepo subsRepo;
    @Autowired
    private UserService userService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyASubscription(@RequestBody Subscription subs) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        Long id = subsService.findById(username);
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent())
            subs.setUser(user.get());
        boolean res = subsService.saveNewSubs(subs);
        if (res) {
            return new ResponseEntity<>("Subscribed Successfully", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("!! Error while Subscribing a pass", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() //get user subscription status
    {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> opUser = userService.findByUsername(username);
        if (opUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        Optional<Subscription> subs = subsRepo.findByUserId(user.getId());
        if (subs.isEmpty()) {
            return ResponseEntity.ok("No active subscription found");
        }

        Subscription subscription = subs.get();

        // check if subscription is still valid
        if (subscription.getEndDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.ok("Subscription expired on: " + subscription.getEndDate());
        }

        return ResponseEntity.ok("Active subscription: " + subscription.getType()
                + " (valid until " + subscription.getEndDate() + ")");

    }
}
