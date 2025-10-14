package com.ecommerce.orderprocessing.shoppingcart.dto;

public record AddCartItemRequest(
        Long productId,
        Integer quantity
) {
}
