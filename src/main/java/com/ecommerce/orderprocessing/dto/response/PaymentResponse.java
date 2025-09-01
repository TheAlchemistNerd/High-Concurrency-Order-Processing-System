package com.ecommerce.orderprocessing.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Record for payment response.
 */
public record PaymentResponse(
        String paymentId,
        String status,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime processedAt,
        String transactionId,
        String message
) {}