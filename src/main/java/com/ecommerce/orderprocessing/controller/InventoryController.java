package com.ecommerce.orderprocessing.controller;

import com.ecommerce.orderprocessing.dto.response.PagedResponse;
import com.ecommerce.orderprocessing.dto.response.ProductResponse;
import com.ecommerce.orderprocessing.service.InventoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/products/{productId}")
    public CompletableFuture<Boolean> checkInventory(@PathVariable Long productId, @RequestParam Integer quantity) {
        return inventoryService.checkInventory(productId, quantity);
    }

    @PostMapping("/products/{productId}/reserve")
    public CompletableFuture<Void> reserveInventory(@PathVariable Long productId, @RequestParam Integer quantity) {
        return inventoryService.reserveInventory(productId, quantity);
    }

    @PostMapping("/products/{productId}/restore")
    public CompletableFuture<Void> restoreInventory(@PathVariable Long productId, @RequestParam Integer quantity) {
        return inventoryService.restoreInventory(productId, quantity);
    }

    @GetMapping("/products/{productId}/inventory")
    public CompletableFuture<ProductResponse> getProductInventory(@PathVariable Long productId) {
        return inventoryService.getProductInventory(productId);
    }

    @GetMapping("/products")
    public CompletableFuture<PagedResponse<ProductResponse>> getAllProducts(Pageable pageable) {
        return inventoryService.getAllProducts(pageable);
    }

    @GetMapping("/products/low-stock")
    public CompletableFuture<List<ProductResponse>> getLowStockProducts(@RequestParam Integer threshold) {
        return inventoryService.getLowStockProducts(threshold);
    }

    @GetMapping("/products/out-of-stock")
    public CompletableFuture<List<ProductResponse>> getOutOfStockProducts() {
        return inventoryService.getOutOfStockProducts();
    }
}
