package com.ecommerce.orderprocessing.inventory.dto;

public record InventoryResponse(
        Long productId,
        Integer stockQuantity
) {}