package com.ecommerce.orderprocessing.product.service;

import com.ecommerce.orderprocessing.product.ProductResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProductCatalogService {
    CompletableFuture<ProductResponse> getProductById(Long productId);
    CompletableFuture<List<ProductResponse>> getAllProducts();
    // Add other methods as per the architecture document (e.g., search, filter)
}