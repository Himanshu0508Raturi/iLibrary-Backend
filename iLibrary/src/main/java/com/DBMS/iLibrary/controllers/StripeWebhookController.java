package com.DBMS.iLibrary.controllers;

import com.DBMS.iLibrary.service.SeatPaymentService;
import com.DBMS.iLibrary.service.StripeWebhookService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@CrossOrigin
public class StripeWebhookController {

    @Value("${stripe.secretKey1}")
    private String secretKey1;

    @Value("${stripe.secretKey2}")
    private String secretKey2;

    @Value("${stripe.seatPaymentWebhookSecret}")
    private String seatPaymentWebhookSecret;

    @Value("${stripe.subscriptionPaymentWebhookSecret}")
    private String subscriptionPaymentWebhookSecret;

    @Autowired
    private StripeWebhookService stripeWebhookService;

    @PostMapping("/seat")
    public ResponseEntity<String> handleStripeWebhookForSeatBooking(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Stripe.apiKey = secretKey1;
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, seatPaymentWebhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body(" Invalid signature");
        }
        try {
            stripeWebhookService.webhookEventForSeatPayment(event);
        } catch (MessagingException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending payment mail to user.");
        }
        return ResponseEntity.ok(" Webhook processed successfully");
    }

    @PostMapping("/subscription")
    public ResponseEntity<?> handleStripeWebhookForSubscription(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Stripe.apiKey = secretKey2;
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, subscriptionPaymentWebhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body(" Invalid signature");
        }
        try {
            stripeWebhookService.webhookEventForSubscriptionPayment(event);
        } catch (MessagingException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending payment mail to user.");
        }
        return ResponseEntity.ok(" Webhook processed successfully");
    }
}
