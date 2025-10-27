package com.DBMS.iLibrary.service;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeWebhookService {
    @Value("${stripe.secretKey}")
    private String secretKey;

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    @Autowired
    private SeatPaymentService seatPaymentService;
    @Autowired
    private MailService mailService;

    public void webhookEvent(Event event) throws MessagingException {
        String eventType = event.getType();
        System.out.println("Received event: " + eventType);

        switch (eventType) {

            //When Checkout session completes successfully
            case "checkout.session.completed": {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .filter(obj -> obj instanceof Session)
                        .map(obj -> (Session) obj)
                        .orElse(null);

                if (session != null) {
                    System.out.println(" Checkout session completed: " + session.getId());
                    seatPaymentService.saveDataAfterPayment(session);
                    mailService.sendPaymentConformMail(event);
                }
                break;
            }

            // Async payment (UPI, netbanking) succeeded
            case "checkout.session.async_payment_succeeded": {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .filter(obj -> obj instanceof Session)
                        .map(obj -> (Session) obj)
                        .orElse(null);

                if (session != null) {
                    System.out.println(" Async payment succeeded: " + session.getId());
                    seatPaymentService.saveDataAfterPayment(session);
                    mailService.sendPaymentConformMail(event);
                }
                break;
            }

            //  Async payment failed or expired
            case "checkout.session.async_payment_failed":
            case "checkout.session.expired": {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .filter(obj -> obj instanceof Session)
                        .map(obj -> (Session) obj)
                        .orElse(null);

                if (session != null) {
                    System.out.println(" Payment failed or session expired: " + session.getId());
                    seatPaymentService.handleBookingPaymentFailure(session);
                    mailService.sendPaymentCancelMail(event);
                }
                break;
            }

//            // Direct payment intent success (some test triggers send this)
//            case "payment_intent.succeeded": {
//                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
//                        .getObject()
//                        .filter(obj -> obj instanceof PaymentIntent)
//                        .map(obj -> (PaymentIntent) obj)
//                        .orElse(null);
//
//                if (paymentIntent != null) {
//                    System.out.println("PaymentIntent succeeded: " + paymentIntent.getId());
//                    // You can log or map this if your flow uses direct PaymentIntent
//                }
//                break;
//            }
//
//            //  Direct payment intent cancelled/failed
//            case "payment_intent.canceled": {
//                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
//                        .getObject()
//                        .filter(obj -> obj instanceof PaymentIntent)
//                        .map(obj -> (PaymentIntent) obj)
//                        .orElse(null);
//
//                if (paymentIntent != null) {
//                    System.out.println(" PaymentIntent canceled: " + paymentIntent.getId());
//                }
//                break;
//            }

            default:
                System.out.println("Unhandled event type: " + eventType);
                break;
        }
    }
}
