package com.ecommerce.orderprocessing.payment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record PaymentStatusResponse(
        String paymentId,
        String status,
        String detailedStatus,
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime lastUpdated,
        String message
) {}
