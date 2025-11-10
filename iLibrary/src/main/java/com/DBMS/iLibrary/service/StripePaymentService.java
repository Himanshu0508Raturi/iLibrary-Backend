package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.PaymentRequest;
import com.DBMS.iLibrary.entity.StripeResponse;
import com.DBMS.iLibrary.entity.User;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentService {

    @Value("${stripe.secretKey1}")
    private String secretKey1;

    @Value("${stripe.secretKey2}")
    private String secretKey2;

    public StripeResponse checkoutProducts(PaymentRequest paymentRequest, User user) {
        // check from which webhook request is coming.
        if(paymentRequest.getName().equals("Seat Payment"))
        {
            Stripe.apiKey = secretKey1;
        }else if (paymentRequest.getName().equals("Subscription Payment")){
            Stripe.apiKey = secretKey2;
        }

        try {
            // Create product data
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(paymentRequest.getName())
                            .build();

            // Create price data (must include productData)
            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(paymentRequest.getCurrency() == null ? "usd" : paymentRequest.getCurrency())
                            .setUnitAmount(paymentRequest.getAmount())
                            .setProductData(productData) // ✅ required
                            .build();

            // Create line item
            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(paymentRequest.getQuantity())
                            .setPriceData(priceData)
                            .build();

            // Build checkout session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("https://dbms-frontend-two.vercel.app/success.html")
                    .setCancelUrl("https://dbms-frontend-two.vercel.app/cancel.html")
                    .addLineItem(lineItem)
                    .putMetadata("userId",user.getId().toString())
                    .putMetadata("username", user.getUsername())
                    .build();

            Session session = Session.create(params);

            // Return successful response
            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Payment session created")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();

        } catch (StripeException e) {
            e.printStackTrace(); // to see exact reason in console
            return StripeResponse.builder()
                    .status("FAILED")
                    .message("Stripe error: " + e.getMessage())
                    .sessionId(null)
                    .sessionUrl(null)
                    .build();
        }
    }
}
