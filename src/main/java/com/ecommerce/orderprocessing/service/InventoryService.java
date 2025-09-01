package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Product;
import com.ecommerce.orderprocessing.dto.response.PagedResponse;
import com.ecommerce.orderprocessing.dto.response.ProductResponse;
import com.ecommerce.orderprocessing.exception.InsufficientStockException;
import com.ecommerce.orderprocessing.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Transactional
@Slf4j
public class InventoryService {
    private final ProductRepository productRepository;
    private final ExecutorService virtualThreadExecutor;

    public InventoryService(ProductRepository productRepository, ExecutorService virtualThreadExecutor) {
        this.productRepository = productRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Transactional(readOnly = true)
    public CompletableFuture<Boolean> checkInventory(Long productId, Integer quantity) {
        return CompletableFuture.supplyAsync(() -> {
            var product = findProductById(productId);
            boolean hasStock = product.hasStock(quantity);
            log.debug("Inventory check for product {}: requested={}, available={}, hasStock={}",
                    productId, quantity, product.getStockQuantity(), hasStock);
            return hasStock;
        }, virtualThreadExecutor);
    }

    public CompletableFuture<Void> reserveInventory(Long productId, Integer quantity) {
        return CompletableFuture.runAsync(() -> {
            var product = findProductById(productId);
            if (!product.hasStock(quantity)) {
                throw new InsufficientStockException(product.getName(), quantity, product.getStockQuantity());
            }
            product.reduceStock(quantity);
            productRepository.save(product);
            log.info("Reserved {} units of product {} (ID: {})", quantity, product.getName(), productId);
        }, virtualThreadExecutor);
    }

    public CompletableFuture<Void> restoreInventory(Long productId, Integer quantity) {
        return CompletableFuture.runAsync(() -> {
            var product = findProductById(productId);
            product.restoreStock(quantity);
            productRepository.save(product);
            log.info("Restored {} units of product {} (ID: {})", quantity, product.getName(), productId);
        }, virtualThreadExecutor);
    }

    @Transactional(readOnly = true)
    public CompletableFuture<ProductResponse> getProductInventory(Long productId) {
        return CompletableFuture.supplyAsync(() -> {
            var product = findProductById(productId);
            return convertToProductResponse(product);
        }, virtualThreadExecutor);
    }

    @Transactional(readOnly = true)
    public CompletableFuture<PagedResponse<ProductResponse>> getAllProducts(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            var productPage = productRepository.findByIsActiveTrue(pageable);
            var productResponses = productPage.getContent().stream()
                    .map(this::convertToProductResponse)
                    .toList();

            return new PagedResponse<>(
                    productResponses,
                    productPage.getNumber(),
                    productPage.getSize(),
                    productPage.getTotalElements(),
                    productPage.getTotalPages()
            );
        }, virtualThreadExecutor);
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<ProductResponse>> getLowStockProducts(Integer threshold) {
        return CompletableFuture.supplyAsync(() -> productRepository.findLowStockProducts(threshold).stream()
                .map(this::convertToProductResponse)
                .toList(), virtualThreadExecutor);
    }

    @Transactional(readOnly = true)
    public CompletableFuture<List<ProductResponse>> getOutOfStockProducts() {
        return CompletableFuture.supplyAsync(() -> productRepository.findOutOfStockProducts().stream()
                .map(this::convertToProductResponse)
                .toList(), virtualThreadExecutor);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    private ProductResponse convertToProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getIsActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
