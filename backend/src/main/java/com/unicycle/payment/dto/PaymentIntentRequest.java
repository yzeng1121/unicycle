package com.unicycle.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentIntentRequest {
    private String listingId;
    private String currency; // defaults to "USD"
}
