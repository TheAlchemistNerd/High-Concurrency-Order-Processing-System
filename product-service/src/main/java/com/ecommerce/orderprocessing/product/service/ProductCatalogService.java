package com.ecommerce.orderprocessing.product.service;

import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.product.dto.ProductRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProductCatalogService {
    CompletableFuture<ProductResponse> getProductById(Long productId);
    CompletableFuture<List<ProductResponse>> getAllProducts();
    CompletableFuture<ProductResponse> createProduct(ProductRequest productRequest);
    CompletableFuture<ProductResponse> updateProduct(Long productId, ProductRequest productRequest);
    CompletableFuture<Void> deleteProduct(Long productId);
}