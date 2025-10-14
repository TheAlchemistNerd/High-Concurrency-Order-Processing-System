package com.ecommerce.orderprocessing.order.dto;

import java.math.BigDecimal;

/**
 * Record for order item response.
 */
public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {}
