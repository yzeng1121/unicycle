package com.unicycle.payment.service;

import com.unicycle.payment.dto.StripeResponse;
import com.unicycle.payment.dto.ProductRequest;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class StripeService {
    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    public StripeResponse checkoutProduct(ProductRequest productRequest) {
        Stripe.apiKey = secretKey;

        SessionCreateParams.LineItem.PriceData.ProductData productData =
            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(productRequest.getName())
                .build();

        SessionCreateParams.LineItem.PriceData priceData =
            SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(productRequest.getCurrency() == null ?
                        "USD" : productRequest.getCurrency())
                .setUnitAmount(productRequest.getAmount())
                .setProductData(productData)
                .build();

        SessionCreateParams.LineItem lineItem =
            SessionCreateParams.LineItem.builder()
                .setQuantity(productRequest.getQuantity())
                .setPriceData(priceData)
                .build();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(lineItem)
                .build();

        try {
            Session session = Session.create(params);
            return StripeResponse.builder()
                    .status("SUCCESS")
                    .message("Payment session created")
                    .sessionId(session.getId())
                    .sessionUrl(session.getUrl())
                    .build();
        } catch (StripeException e) {
            return StripeResponse.builder()
                    .status("FAILURE")
                    .message(e.getMessage())
                    .build();
        }
    }
}
