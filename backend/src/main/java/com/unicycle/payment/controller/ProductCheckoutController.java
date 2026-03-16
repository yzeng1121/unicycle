package com.unicycle.payment.controller;

import com.unicycle.payment.dto.ProductRequest;
import com.unicycle.payment.dto.StripeResponse;
import com.unicycle.payment.service.StripeService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/product")
@AllArgsConstructor
public class ProductCheckoutController {
    private StripeService stripeService;

    // TODO: stripe transaction when jwt token expires
    @PostMapping("/checkout")
    public ResponseEntity<StripeResponse> 
        checkoutProducts(@RequestBody ProductRequest productRequest) {

        StripeResponse stripeResponse = 
            stripeService.checkoutProduct(productRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stripeResponse);
    }
}
