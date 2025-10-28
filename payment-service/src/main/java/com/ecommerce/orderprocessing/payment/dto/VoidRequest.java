package com.ecommerce.orderprocessing.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record VoidRequest(
        @NotBlank(message = "Authorization ID is required")
        String authorizationId,

        String reason,
        String idempotencyKey
) {}
