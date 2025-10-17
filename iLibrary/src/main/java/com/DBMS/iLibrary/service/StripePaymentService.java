package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.PaymentRequest;
import com.DBMS.iLibrary.entity.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentService {

    @Value("${stripe.secretKey}")
    private String secretKey;

    public StripeResponse checkoutProducts(PaymentRequest paymentRequest) {
        Stripe.apiKey = secretKey;

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
                    .setSuccessUrl("http://localhost:8080/success.html")
                    .setCancelUrl("http://localhost:8080/cancel.html")
                    .addLineItem(lineItem)
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
