package com.ecommerce.orderprocessing.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CaptureRequest(
        @NotBlank(message = "Authorization ID is required")
        String authorizationId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Capture amount must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Capture amount format is invalid")
        BigDecimal amount,

        String idempotencyKey
) {}
