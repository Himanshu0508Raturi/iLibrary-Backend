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
import java.util.Optional;

@RestController
@RequestMapping("/subscription")
public class SubscriptionController {
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
    @PutMapping("/renew")
    public ResponseEntity<?> renewASubs()
    {
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(opUser.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        // 2. Find user's active subscription
        Optional<Subscription> opSubs = subsService.getActiveSubscription(user);
        if (opSubs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active subscription found for this user");
        }

        Subscription subs = opSubs.get();

        // 3. Extend subscription based on type
        switch (subs.getType()) {
            case WEEKLY:
                subs.setEndDate(subs.getEndDate().plusDays(7));
                break;
            case MONTHLY:
                subs.setEndDate(subs.getEndDate().plusMonths(1));
                break;
            case YEARLY:
                subs.setEndDate(subs.getEndDate().plusYears(1));
                break;
        }

        subs.setStatus(Subscription.SubscriptionStatus.ACTIVE); // ensure it remains active

        // 4. Save updated subscription
        subsRepo.save(subs);

        // 5. Return success response
        return ResponseEntity.ok("Subscription renewed successfully. New end date: " + subs.getEndDate());
    }
    @PutMapping("/cancel")
    public ResponseEntity<?> cancelASubs()
    {
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if(opUser.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        // 2. Find user's active subscription
        Optional<Subscription> opSubs = subsService.getActiveSubscription(user);
        if (opSubs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active subscription found for this user");
        }
        Subscription subs = opSubs.get();
        subs.setStatus(Subscription.SubscriptionStatus.valueOf("PASSIVE"));
        subsRepo.save(subs);
        return ResponseEntity.ok("Subscription Cancelled successfully.");
    }
}
