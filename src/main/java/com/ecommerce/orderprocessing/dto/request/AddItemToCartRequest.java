package com.ecommerce.orderprocessing.dto.request;

public record AddItemToCartRequest(
        Long productId,
        Integer quantity
) {
}
