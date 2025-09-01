package com.ecommerce.orderprocessing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

// Request DTOs

/**
 * Record for creating a new order.
 */
public record CreateOrderRequest(
        @NotNull(message = "Customer ID is required")
        Long customerId,

        @NotBlank(message = "Shipping address is required")
        @Size(max = 500, message = "Shipping address must not exceed 500 characters")
        String shippingAddress,

        @NotEmpty(message = "Order items cannot be empty")
        @Valid
        List<CreateOrderItemRequest> orderItems,

        @Size(max = 1000, message = "Notes must not exceed 1000 characters")
        String notes
) {}

