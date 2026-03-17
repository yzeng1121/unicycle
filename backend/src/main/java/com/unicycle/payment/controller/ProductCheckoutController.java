package com.unicycle.payment.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unicycle.auth.entity.User;
import com.unicycle.payment.dto.PaymentIntentRequest;
import com.unicycle.payment.dto.PaymentIntentResponse;
import com.unicycle.payment.service.StripeService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductCheckoutController {

    private final StripeService stripeService;

    @PostMapping("/checkout")
    public ResponseEntity<PaymentIntentResponse> checkout(
            @RequestBody PaymentIntentRequest request,
            @AuthenticationPrincipal User buyer) {

        UUID listingId = UUID.fromString(request.getListingId());
        UUID buyerId = buyer.getUserId();

        PaymentIntentResponse response = stripeService.createPaymentIntent(
                listingId, buyerId, request.getCurrency());

        HttpStatus status = "SUCCESS".equals(response.getStatus())
                ? HttpStatus.OK
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }
}
