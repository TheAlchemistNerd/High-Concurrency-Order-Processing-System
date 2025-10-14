package com.ecommerce.orderprocessing.product.controller;

import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;

    public ProductController(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    @GetMapping("/{productId}")
    public CompletableFuture<ProductResponse> getProductById(@PathVariable Long productId) {
        return productCatalogService.getProductById(productId);
    }

    @GetMapping
    public CompletableFuture<List<ProductResponse>> getAllProducts() {
        return productCatalogService.getAllProducts();
    }

    // TODO: Add other API operations as per the architecture document (e.g., search, filter, admin endpoints)
}