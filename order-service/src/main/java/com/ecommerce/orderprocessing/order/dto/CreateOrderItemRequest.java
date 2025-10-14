package com.ecommerce.orderprocessing.order.dto;

import jakarta.validation.constraints.*;

/**
 * Record for creating order items.
 */
public record CreateOrderItemRequest(
        @NotNull(message = "Product ID is required")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 1000, message = "Quantity cannot exceed 1000")
        Integer quantity
) {}

