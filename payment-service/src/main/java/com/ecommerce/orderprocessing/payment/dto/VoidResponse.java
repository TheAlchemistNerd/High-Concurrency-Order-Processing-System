package com.ecommerce.orderprocessing.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record VoidResponse(
        String authorizationId,
        String status,
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime voidedAt,
        String message
) {}
