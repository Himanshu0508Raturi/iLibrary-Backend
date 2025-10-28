package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.entity.*;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.repository.SubscriptionRepo;
import com.DBMS.iLibrary.service.SeatPaymentService;
import com.DBMS.iLibrary.service.StripePaymentService;
import com.DBMS.iLibrary.service.UserService;
import com.DBMS.iLibrary.service.subscriptionPaymentService;
import org.hibernate.NonUniqueResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
@CrossOrigin
public class PriceCheckoutController {
    @Autowired
    private StripePaymentService stripePaymentService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private SeatPaymentService seatPaymentService;
    @Autowired
    private SubscriptionRepo subscriptionRepo;
    @Autowired
    private subscriptionPaymentService subscriptionPaymentService;

    // for seatPayment
    @GetMapping("/seat")
    public ResponseEntity<?> seatPaymentCheckout() {
        // 1. Validate authentication and find user
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (opUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        // 2. Find pending booking
        List<Booking> bookings = bookingRepo.findAllByUserIdAndStatus(user.getId(), Booking.BookingStatus.PENDING);
        if (bookings.size() > 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Multiple pending bookings found for user: " + user.getUsername());
        } else if (bookings.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No pending booking found for user: " + user.getUsername());
        }

        Booking booking = bookings.get(0);

        // 3 Create payment request
        double basePrice = 50.00;
        double totalAmount = 50.00 * booking.getHrs();
        double gstPerPerson = (totalAmount * 0.18)/booking.getHrs();
        double amtToSet = basePrice+ gstPerPerson;
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setName("Seat Payment");
        paymentRequest.setCurrency("INR");
        paymentRequest.setQuantity((long) booking.getHrs());
        paymentRequest.setAmount((long) (amtToSet * 100));
        // 4. Call Stripe service
        StripeResponse stripeResponse = stripePaymentService.checkoutProducts(paymentRequest, user);
        //calling SeatPaymentService.saveDataBeforePayment() so that data was saved in db.
        try {
            seatPaymentService.saveDataBeforePayment(booking, stripeResponse.getSessionId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("RuntimeError while saving entries to SeatPayment table.");
        }
        // 5. Return structured response
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }

//    // for subscription controller
    @GetMapping("/subscription")
    public ResponseEntity<?> subscriptionPaymentCheckout()
    {
        // 1. Validate authentication and find user
        Optional<User> opUser = userService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (opUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = opUser.get();
        // 2. Find PASSIVE subscription
        List<Subscription> allSubscription = subscriptionRepo.findAllByUserAndStatus(user, Subscription.SubscriptionStatus.PASSIVE);
        if(allSubscription.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No subscription found for user: "+user.getUsername()+".");
        }
        Subscription subscription = allSubscription.get(allSubscription.size() - 1);

        // 3 Create payment request
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setName("Subscription Payment");
        paymentRequest.setAmount(subscription.getAmount().longValue() * 100);
        paymentRequest.setQuantity(1L);
        paymentRequest.setCurrency("INR");
        // 4. Call Stripe service
        StripeResponse stripeResponse = stripePaymentService.checkoutProducts(paymentRequest, user);
        //calling subscriptionPaymentService.saveDataBeforePayment() so that data was saved in db.
        try{
            subscriptionPaymentService.saveDataBeforePayment(subscription, stripeResponse.getSessionId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("RuntimeError while saving entries to SeatPayment table.");
        }
        // 5. Return structured response
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }
}
