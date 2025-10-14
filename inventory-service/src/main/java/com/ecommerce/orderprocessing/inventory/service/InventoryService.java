package com.ecommerce.orderprocessing.inventory.service;

import com.ecommerce.orderprocessing.inventory.dto.InventoryResponse;

import java.util.concurrent.CompletableFuture;

public interface InventoryService {
    CompletableFuture<Boolean> checkInventory(Long productId, Integer quantity);
    CompletableFuture<Void> reserveInventory(Long productId, Integer quantity);
    CompletableFuture<Void> releaseInventory(Long productId, Integer quantity);
    CompletableFuture<Void> commitInventory(Long productId, Integer quantity);
    CompletableFuture<InventoryResponse> getInventoryByProductId(Long productId);
    // Add other methods as needed, e.g., for admin operations
}