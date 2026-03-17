package com.unicycle.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private String message;
}
