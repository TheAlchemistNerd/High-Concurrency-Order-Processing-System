package com.ecommerce.orderprocessing.product.controller;

import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.product.dto.ProductRequest;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductCatalogService productCatalogService;
    private final ProductModelAssembler assembler;

    public ProductController(ProductCatalogService productCatalogService, ProductModelAssembler assembler) {
        this.productCatalogService = productCatalogService;
        this.assembler = assembler;
    }

    @GetMapping("/{productId}")
    public CompletableFuture<EntityModel<ProductResponse>> getProductById(@PathVariable Long productId) {
        return productCatalogService.getProductById(productId)
                .thenApply(assembler::toModel);
    }

    @GetMapping
    public CompletableFuture<CollectionModel<EntityModel<ProductResponse>>> getAllProducts() {
        return productCatalogService.getAllProducts()
                .thenApply(products -> {
                    List<EntityModel<ProductResponse>> productModels = products.stream()
                            .map(assembler::toModel)
                            .collect(Collectors.toList());
                    return CollectionModel.of(productModels,
                            linkTo(methodOn(ProductController.class).getAllProducts()).withSelfRel());
                });
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    public CompletableFuture<ResponseEntity<EntityModel<ProductResponse>>> createProduct(@RequestBody ProductRequest productRequest) {
        return productCatalogService.createProduct(productRequest)
                .thenApply(assembler::toModel)
                .thenApply(productModel -> {
                    return ResponseEntity
                            .created(productModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                            .body(productModel);
                });
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    public CompletableFuture<ResponseEntity<EntityModel<ProductResponse>>> updateProduct(@PathVariable Long productId, @RequestBody ProductRequest productRequest) {
        return productCatalogService.updateProduct(productId, productRequest)
                .thenApply(assembler::toModel)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")
    public CompletableFuture<ResponseEntity<Void>> deleteProduct(@PathVariable Long productId) {
        return productCatalogService.deleteProduct(productId)
                .thenApply(__ -> ResponseEntity.noContent().build());
    }
}