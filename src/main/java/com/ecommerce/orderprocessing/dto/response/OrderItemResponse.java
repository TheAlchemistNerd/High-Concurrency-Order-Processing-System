package com.ecommerce.orderprocessing.dto.response;

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
