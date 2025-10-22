package com.ecommerce.orderprocessing.shoppingcart.controller;

import com.ecommerce.orderprocessing.shoppingcart.dto.AddCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.UpdateCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.ShoppingCartResponse;
import com.ecommerce.orderprocessing.shoppingcart.service.ShoppingCartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")
    public CompletableFuture<ShoppingCartResponse> getShoppingCart(@PathVariable Long customerId, Authentication authentication) {
        return shoppingCartService.getShoppingCart(customerId);
    }

    @PostMapping("/{customerId}/items")
    @PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")
    public CompletableFuture<ShoppingCartResponse> addItemToCart(@PathVariable Long customerId, @RequestBody AddCartItemRequest request, Authentication authentication) {
        return shoppingCartService.addItemToCart(customerId, request);
    }

    @PutMapping("/{customerId}/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")
    public CompletableFuture<ShoppingCartResponse> updateItemQuantity(@PathVariable Long customerId, @PathVariable Long productId, @RequestBody UpdateCartItemRequest request, Authentication authentication) {
        return shoppingCartService.updateItemQuantity(customerId, productId, request);
    }

    @DeleteMapping("/{customerId}/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")
    public CompletableFuture<ShoppingCartResponse> removeItemFromCart(@PathVariable Long customerId, @PathVariable Long productId, Authentication authentication) {
        return shoppingCartService.removeItemFromCart(customerId, productId);
    }

    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")
    public CompletableFuture<Void> clearShoppingCart(@PathVariable Long customerId, Authentication authentication) {
        return shoppingCartService.clearShoppingCart(customerId);
    }
}
