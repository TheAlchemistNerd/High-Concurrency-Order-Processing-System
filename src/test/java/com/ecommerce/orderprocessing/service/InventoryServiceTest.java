package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Product;
import com.ecommerce.orderprocessing.dto.response.ProductResponse;
import com.ecommerce.orderprocessing.exception.InsufficientStockException;
import com.ecommerce.orderprocessing.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService(productRepository, virtualThreadExecutor);
    }

    @Test
    void checkInventory_shouldReturnTrue() throws Exception {
        // Given
        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        CompletableFuture<Boolean> future = inventoryService.checkInventory(1L, 5);
        Boolean result = future.get();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void reserveInventory_shouldReserveStock() throws Exception {
        // Given
        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        inventoryService.reserveInventory(1L, 5).get();

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(5);
    }

    @Test
    void reserveInventory_whenInsufficientStock_shouldThrowException() {
        // Given
        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        CompletableFuture<Void> future = inventoryService.reserveInventory(1L, 10);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(InsufficientStockException.class);
    }

    @Test
    void restoreInventory_shouldRestoreStock() throws Exception {
        // Given
        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        inventoryService.restoreInventory(1L, 5).get();

        // Then
        assertThat(product.getStockQuantity()).isEqualTo(10);
    }

    @Test
    void getProductInventory_shouldReturnProductResponse() throws Exception {
        // Given
        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // When
        CompletableFuture<ProductResponse> future = inventoryService.getProductInventory(1L);
        ProductResponse productResponse = future.get();

        // Then
        assertThat(productResponse).isNotNull();
        assertThat(productResponse.name()).isEqualTo("Test Product");
    }
}
