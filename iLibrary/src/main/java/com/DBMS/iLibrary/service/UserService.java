package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private SubscriptionRepo subscriptionRepo;
    @Autowired
    private SeatPaymentRepository seatPaymentRepository;
    @Autowired
    private SubscriptionPaymentRepository subscriptionPaymentRepository;

    private static final PasswordEncoder passwordencoder = new BCryptPasswordEncoder();
    @Transactional
    public RuntimeException saveUser(User user, Set<String> role) {
        try {
            user.setPassword(passwordencoder.encode(user.getPassword()));
            user.setCreatedAt(new Date());
            user.setRoles(role);
            userRepository.save(user);
        } catch (Exception e) {
            return new RuntimeException("Error while saving user");
        }
        return null;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    /* public boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(gmail|outlook)\\.com$";
        return email.matches(emailRegex);
    }
    public boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        // Regex: start with letter, followed by letters, digits or underscore, total length 6-30
        String usernameRegex = "^[A-Za-z][A-Za-z0-9_]{5,29}$";
        return username.matches(usernameRegex);
    } Used @Valid instead this. */

    public List<Booking> getUserBookingHistory(User user) {
        return bookingRepo.findAllByUserId(user.getId());
    }

    public Subscription getActiveSubscriptionOfUser(User user) {
        Optional<Subscription> OpSubscription = subscriptionRepo.findByUserAndStatus(user, Subscription.SubscriptionStatus.valueOf("ACTIVE"));
        if (OpSubscription.isEmpty()) {
            throw new RuntimeException("No Subscription found for user: "+ user.getUsername());
        }
        return OpSubscription.get();
    }
    public List<Subscription> getAllSubscriptionOfUser(User user) {
        List<Subscription> allSubscription = subscriptionRepo.findAllByUser(user);
        if (allSubscription.isEmpty()) {
            throw new RuntimeException("No Subscription found for user: "+ user.getUsername());
        }
        return allSubscription;
    }
    @Transactional
    public void deleteAUser(User user)
    {
        bookingRepo.deleteByUserId(user.getId());
        seatPaymentRepository.deleteByUserId(user.getId());
        subscriptionPaymentRepository.deleteByUserId(user.getId());
        subscriptionRepo.deleteByUserId(user.getId());
        userRepository.delete(user);
    }
}
