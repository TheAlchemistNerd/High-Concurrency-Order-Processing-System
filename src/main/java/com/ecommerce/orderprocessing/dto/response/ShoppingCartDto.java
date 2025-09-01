package com.ecommerce.orderprocessing.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ShoppingCartDto(
        Long id,
        Long customerId,
        List<ShoppingCartItemDto> items,
        BigDecimal totalAmount
) {
}
