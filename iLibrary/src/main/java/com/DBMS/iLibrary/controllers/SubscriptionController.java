package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.SubscriptionRepo;
import com.DBMS.iLibrary.service.MailService;
import com.DBMS.iLibrary.service.SubscriptionService;
import com.DBMS.iLibrary.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@RestController
@RequestMapping("/subscription")
@CrossOrigin
public class SubscriptionController {
    @Autowired
    private SubscriptionService subsService;

    @Autowired
    private SubscriptionRepo subsRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private MailService mailService;

    // Buy's a subscription for a logged-in user.Save logged-in user's entity to subscription entity field and then save to
    // subscription table in DB.
    // RequestBody -> Subscription entity
    // Mail service involved.
    @PostMapping("/buy")
    public ResponseEntity<?> buyASubscription(@Valid @RequestBody Subscription subs) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        Long id = subsService.findById(username);
        Optional<User> user = userService.findByUsername(username);
        user.ifPresent(subs::setUser);
//        if (user.isPresent())
//            subs.setUser(user.get());
        boolean res = subsService.saveNewSubs(subs);
        if (res) {
            try {
                mailService.sendSubscriptionMail(user.get(), subs);
            } catch (MessagingException e) {
                return new ResponseEntity<>("!! Error while sending buy mail to User: " + user.get().getUsername() + ".", HttpStatus.BAD_REQUEST);
            }
            return new ResponseEntity<>("Subscribed Successfully", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("!! Error while Subscribing a pass", HttpStatus.BAD_REQUEST);
    }

    // get subscription entity belongs to a particular logged-in user from subscription table.
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
        String endDate = subscription.getEndDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));
        return ResponseEntity.ok("Active subscription: " + subscription.getType()
                + " (valid until " + endDate + ")");
    }

    // renew subscription of a user if he/she wants , change end date of subscription only.
    // Mail service involved
    @PutMapping("/renew")
    public ResponseEntity<?> renewASubs() {
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (opUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        // 2. Find user's active subscription
        Optional<Subscription> opSubs = subsService.getActiveSubscription(user);
        if (opSubs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active subscription found for the user: " + user.getUsername() + ".");
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
        try {
            mailService.renewSubscriptionMail(user, subs);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Error while Sending renew subscription mail to user: " + user.getUsername() + ".", HttpStatus.BAD_REQUEST);
        }
        // 5. Return success response
        return ResponseEntity.ok("Subscription renewed successfully. New end date: " + subs.getEndDate());
    }

    // Cancel a subscription for a user if he/she wants. don't delete from subscription table just change status from ACTIVE to
    // PASSIVE and save back to subscription table.
    // Mail service involved.
    @PutMapping("/cancel")
    public ResponseEntity<?> cancelASubs() {
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (opUser.isEmpty()) {
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
        try {
            mailService.cancelSubscriptionMail(user, subs);
        } catch (MessagingException e) {
            return new ResponseEntity<>("Error while Sending Cancellation Mail to user " + user.getUsername() + ".", HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Subscription Cancelled successfully.");
    }
}
