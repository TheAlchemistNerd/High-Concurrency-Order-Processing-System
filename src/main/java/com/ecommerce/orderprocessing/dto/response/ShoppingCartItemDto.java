package com.ecommerce.orderprocessing.dto.response;

import java.math.BigDecimal;

public record ShoppingCartItemDto(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
