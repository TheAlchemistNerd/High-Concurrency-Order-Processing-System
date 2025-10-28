package com.ecommerce.orderprocessing.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RefundRequest(
        @NotBlank(message = "Payment ID is required for refund")
        String paymentId,

        @NotNull(message = "Amount is required for refund")
        @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Refund amount format is invalid")
        BigDecimal amount,

        String reason,
        String idempotencyKey
) {}
