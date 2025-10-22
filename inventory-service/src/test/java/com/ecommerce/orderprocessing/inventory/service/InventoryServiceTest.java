package com.ecommerce.orderprocessing.inventory.service;

import com.ecommerce.orderprocessing.inventory.domain.Inventory;
import com.ecommerce.orderprocessing.inventory.dto.InventoryResponse;
import com.ecommerce.orderprocessing.inventory.exception.InsufficientStockException;
import com.ecommerce.orderprocessing.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    private ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private InventoryServiceImpl inventoryService; // Changed to InventoryServiceImpl

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(inventoryRepository, virtualThreadExecutor); // Changed constructor
    }

    @Test
    void checkInventory_shouldReturnTrue() throws Exception {
        // Given
        Inventory inventory = new Inventory(1L, 10);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        // When
        CompletableFuture<Boolean> future = inventoryService.checkInventory(1L, 5);
        Boolean result = future.get();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void reserveInventory_shouldReserveStock() throws Exception {
        // Given
        Inventory inventory = new Inventory(1L, 10);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        inventoryService.reserveInventory(1L, 5).get();

        // Then
        assertThat(inventory.getStockQuantity()).isEqualTo(5);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void reserveInventory_whenInsufficientStock_shouldThrowException() {
        // Given
        Inventory inventory = new Inventory(1L, 5);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        // When
        CompletableFuture<Void> future = inventoryService.reserveInventory(1L, 10);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(InsufficientStockException.class);
    }

    @Test
    void restoreInventory_shouldRestoreStock() throws Exception {
        // Given
        Inventory inventory = new Inventory(1L, 5);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        // When
        inventoryService.releaseInventory(1L, 5).get();

        // Then
        assertThat(inventory.getStockQuantity()).isEqualTo(10);
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void getInventoryByProductId_shouldReturnInventoryResponse() throws Exception {
        // Given
        Inventory inventory = new Inventory(1L, 10);
        when(inventoryRepository.findByProductId(1L)).thenReturn(Optional.of(inventory));

        // When
        CompletableFuture<InventoryResponse> future = inventoryService.getInventoryByProductId(1L);
        InventoryResponse inventoryResponse = future.get();

        // Then
        assertThat(inventoryResponse).isNotNull();
        assertThat(inventoryResponse.productId()).isEqualTo(1L);
        assertThat(inventoryResponse.stockQuantity()).isEqualTo(10);
    }
}
