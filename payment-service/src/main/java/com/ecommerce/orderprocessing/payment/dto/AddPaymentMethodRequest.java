package com.ecommerce.orderprocessing.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record AddPaymentMethodRequest(
        @NotBlank(message = "Customer ID is required")
        String customerId,

        @NotBlank(message = "Payment method type is required")
        String paymentMethodType,

        // Token from client-side SDK (e.g., Stripe Token, PayPal Nonce)
        @NotBlank(message = "Payment method token is required")
        String token,

        String cardLastFour,
        String cardBrand,
        Integer cardExpMonth,
        Integer cardExpYear
) {}
