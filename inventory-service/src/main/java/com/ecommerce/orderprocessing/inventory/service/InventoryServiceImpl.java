package com.ecommerce.orderprocessing.inventory.service;

import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.inventory.domain.Inventory;
import com.ecommerce.orderprocessing.inventory.dto.InventoryResponse;
import com.ecommerce.orderprocessing.inventory.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ExecutorService virtualThreadExecutor;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ExecutorService virtualThreadExecutor) {
        this.inventoryRepository = inventoryRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<Boolean> checkInventory(Long productId, Integer quantity) {
        return CompletableFuture.supplyAsync(() -> {
            Inventory inventory = findInventoryByProductId(productId);
            boolean hasStock = inventory.hasStock(quantity);
            log.debug("Inventory check for product {}: requested={}, available={}, hasStock={}",
                    productId, quantity, inventory.getStockQuantity(), hasStock);
            return hasStock;
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<Void> reserveInventory(Long productId, Integer quantity) {
        return CompletableFuture.runAsync(() -> {
            Inventory inventory = findInventoryByProductId(productId);
            inventory.reduceStock(quantity);
            inventoryRepository.save(inventory);
            log.info("Reserved {} units of product {} (ID: {})", quantity, productId, productId);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<Void> releaseInventory(Long productId, Integer quantity) {
        return CompletableFuture.runAsync(() -> {
            Inventory inventory = findInventoryByProductId(productId);
            inventory.restoreStock(quantity);
            inventoryRepository.save(inventory);
            log.info("Restored {} units of product {} (ID: {})", quantity, productId, productId);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<Void> commitInventory(Long productId, Integer quantity) {
        return CompletableFuture.runAsync(() -> {
            Inventory inventory = findInventoryByProductId(productId);
            inventory.commitStock(quantity);
            inventoryRepository.save(inventory);
            log.info("Committed {} units of product {} (ID: {})", quantity, productId, productId);
        }, virtualThreadExecutor);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletableFuture<InventoryResponse> getInventoryByProductId(Long productId) {
        return CompletableFuture.supplyAsync(() -> {
            Inventory inventory = findInventoryByProductId(productId);
            return toInventoryResponse(inventory);
        }, virtualThreadExecutor);
    }

    private Inventory findInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));
    }

    private InventoryResponse toInventoryResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getStockQuantity()
        );
    }
}