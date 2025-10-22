package com.ecommerce.orderprocessing.inventory.controller;

import com.ecommerce.orderprocessing.inventory.dto.InventoryResponse;
import com.ecommerce.orderprocessing.inventory.service.InventoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}/check")
    public CompletableFuture<Boolean> checkInventory(@PathVariable Long productId, @RequestParam Integer quantity) {
        return inventoryService.checkInventory(productId, quantity);
    }

    @PostMapping("/products/{productId}/reserve")
    @PreAuthorize("hasRole('ORDER_MANAGER')")
    public CompletableFuture<Void> reserveInventory(@PathVariable Long productId, @RequestParam Integer quantity) {
        return inventoryService.reserveInventory(productId, quantity);
    }

    @PostMapping("/products/{productId}/release")
    @PreAuthorize("hasRole('ORDER_MANAGER')")
    public CompletableFuture<Void> releaseInventory(@PathVariable Long productId, @RequestParam Integer quantity) {
        return inventoryService.releaseInventory(productId, quantity);
    }

    @PostMapping("/products/{productId}/commit")
    @PreAuthorize("hasRole('ORDER_MANAGER')")
    public CompletableFuture<Void> commitInventory(@PathVariable Long productId, @RequestParam Integer quantity) {
        return inventoryService.commitInventory(productId, quantity);
    }

    @PostMapping("/products/{productId}/restock")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    public CompletableFuture<Void> restockInventory(@PathVariable Long productId, @RequestParam Integer quantity) {
        return inventoryService.restockInventory(productId, quantity);
    }

    @GetMapping("/products/{productId}")
    public CompletableFuture<InventoryResponse> getInventoryByProductId(@PathVariable Long productId) {
        return inventoryService.getInventoryByProductId(productId);
    }
}
