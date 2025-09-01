package com.ecommerce.orderprocessing.dto.request;

import jakarta.validation.constraints.*;

/**
 * Record for updating order status.
 */
public record UpdateOrderStatusRequest(
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "PENDING|PAID|PROCESSING|SHIPPED|DELIVERED|CANCELLED",
                message = "Status must be one of: PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED")
        String status,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {}
