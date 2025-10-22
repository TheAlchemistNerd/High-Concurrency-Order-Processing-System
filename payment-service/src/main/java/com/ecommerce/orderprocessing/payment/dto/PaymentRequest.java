package com.ecommerce.orderprocessing.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Record for payment processing.
 */
public record PaymentRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Amount format is invalid")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters")
        String currency,

        // Payment method specific fields (would be encrypted in real implementation)
        String cardNumber,
        String cardHolderName,
        String expiryMonth,
        String expiryYear,
        String cvv
) {
    // Custom constructor to set default currency
    public PaymentRequest(Long orderId, String paymentMethod, BigDecimal amount,
                          String cardNumber, String cardHolderName, String expiryMonth,
                          String expiryYear, String cvv) {
        this(orderId, paymentMethod, amount, "USD",
                cardNumber, cardHolderName, expiryMonth,
                expiryYear, cvv);
    }
}

