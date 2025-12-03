package com.DBMS.iLibrary.service;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StripeWebhookService {
    @Autowired
    private SeatPaymentService seatPaymentService;
    @Autowired
    private MailService mailService;
    @Autowired
    private subscriptionPaymentService subscriptionPaymentService;

    public void webhookEventForSeatPayment(Event event) throws MessagingException {
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

            // Async payment (UPI, net banking) succeeded
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
            default:
                System.out.println("Unhandled event type: " + eventType);
                break;
        }
    }

    public void webhookEventForSubscriptionPayment(Event event) throws MessagingException {
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
                    subscriptionPaymentService.saveDataAfterPayment(session);
                    mailService.sendSubscriptionPaymentConformMail(event);
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
                    subscriptionPaymentService.saveDataAfterPayment(session);
                    mailService.sendSubscriptionPaymentConformMail(event);
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
                    subscriptionPaymentService.handleBookingPaymentFailure(session);
                    mailService.sendSubscriptionPaymentCancelMail(event);
                }
                break;
            }
            default:
                System.out.println("Unhandled event type: " + eventType);
                break;
        }
    }
}
