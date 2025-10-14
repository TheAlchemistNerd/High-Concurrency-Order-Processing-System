package com.ecommerce.orderprocessing.shoppingcart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShoppingCartResponse(
        Long id,
        Long customerId,
        String customerName,
        String customerEmail,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}