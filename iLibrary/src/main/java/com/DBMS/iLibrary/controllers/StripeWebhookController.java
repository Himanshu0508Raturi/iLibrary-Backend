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

    @Value("${stripe.secretKey}")
    private String secretKey;

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    @Autowired
    private StripeWebhookService stripeWebhookService;

    @PostMapping
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Stripe.apiKey = secretKey;
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body(" Invalid signature");
        }
        try {
            stripeWebhookService.webhookEvent(event);
        } catch (MessagingException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while sending payment mail to user.");
        }
        return ResponseEntity.ok(" Webhook processed successfully");
    }
}
