package com.unicycle.payment.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.unicycle.payment.service.StripeService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class WebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final StripeService stripeService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader) throws IOException {

        byte[] rawBody = request.getInputStream().readAllBytes();
        String payload = new String(rawBody, StandardCharsets.UTF_8);

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Invalid signature.");
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = deserializer.getObject().orElse(null);

            if (stripeObject instanceof PaymentIntent paymentIntent) {
                stripeService.handlePaymentIntentSucceeded(paymentIntent);
            }
        }

        return ResponseEntity.ok("Received.");
    }
}
