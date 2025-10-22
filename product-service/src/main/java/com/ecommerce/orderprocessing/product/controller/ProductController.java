package com.ecommerce.orderprocessing.product.controller;

import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.product.dto.ProductRequest;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    public CompletableFuture<ResponseEntity<ProductResponse>> createProduct(@RequestBody ProductRequest productRequest) {
        return productCatalogService.createProduct(productRequest)
                .thenApply(product -> new ResponseEntity<>(product, HttpStatus.CREATED));
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    public CompletableFuture<ResponseEntity<ProductResponse>> updateProduct(@PathVariable Long productId, @RequestBody ProductRequest productRequest) {
        return productCatalogService.updateProduct(productId, productRequest)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    public CompletableFuture<ResponseEntity<Void>> deleteProduct(@PathVariable Long productId) {
        return productCatalogService.deleteProduct(productId)
                .thenApply(__ -> ResponseEntity.noContent().build());
    }
}