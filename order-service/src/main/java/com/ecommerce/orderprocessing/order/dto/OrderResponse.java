package com.ecommerce.orderprocessing.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Record for order response.
 */
public record OrderResponse(
        Long id,
        Long customerId,
        String customerName,
        String customerEmail,
        String status,
        BigDecimal totalAmount,
        String shippingAddress,
        String paymentId,
        String notes,
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        List<OrderItemResponse> orderItems
) {}