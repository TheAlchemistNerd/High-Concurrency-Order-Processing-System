package com.ecommerce.orderprocessing.product.service;

import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.product.Product;
import com.ecommerce.orderprocessing.product.ProductRepository;
import com.ecommerce.orderprocessing.product.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private final ProductRepository productRepository;
    private final ExecutorService virtualThreadExecutor;

    public ProductCatalogServiceImpl(ProductRepository productRepository, ExecutorService virtualThreadExecutor) {
        this.productRepository = productRepository;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    public CompletableFuture<ProductResponse> getProductById(Long productId) {
        return CompletableFuture.supplyAsync(() -> {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
            return toProductResponse(product);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<List<ProductResponse>> getAllProducts() {
        return CompletableFuture.supplyAsync(() -> {
            List<Product> products = productRepository.findAll();
            return products.stream()
                    .map(this::toProductResponse)
                    .collect(Collectors.toList());
        }, virtualThreadExecutor);
    }

    private ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                // stockQuantity will be removed from ProductResponse later
                product.getStockQuantity(), // Temporary, will be removed
                product.getIsActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}