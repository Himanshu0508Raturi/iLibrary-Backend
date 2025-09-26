package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.repository.SubscriptionRepo;
import com.DBMS.iLibrary.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private SubscriptionRepo subscriptionRepo;

    @GetMapping("/allSubscription")
    public ResponseEntity<?> getAllSubs()
    {
        List<Subscription> allSubs = subscriptionRepo.findAll();
        if(!allSubs.isEmpty()) {
            return new ResponseEntity<>(allSubs, HttpStatus.OK);
        }
        return new ResponseEntity<>("No Active Subscription for any user.", HttpStatus.NO_CONTENT);
    }
}
