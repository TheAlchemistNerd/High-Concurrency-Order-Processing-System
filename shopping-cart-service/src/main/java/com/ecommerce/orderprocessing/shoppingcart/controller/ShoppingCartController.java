package com.ecommerce.orderprocessing.shoppingcart.controller;

import com.ecommerce.orderprocessing.shoppingcart.dto.AddCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.UpdateCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.ShoppingCartResponse;
import com.ecommerce.orderprocessing.shoppingcart.service.ShoppingCartService;
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
    public CompletableFuture<ShoppingCartResponse> getShoppingCart(@PathVariable Long customerId) {
        return shoppingCartService.getShoppingCart(customerId);
    }

    @PostMapping("/{customerId}/items")
    public CompletableFuture<ShoppingCartResponse> addItemToCart(@PathVariable Long customerId, @RequestBody AddCartItemRequest request) {
        return shoppingCartService.addItemToCart(customerId, request);
    }

    @PutMapping("/{customerId}/items/{productId}")
    public CompletableFuture<ShoppingCartResponse> updateItemQuantity(@PathVariable Long customerId, @PathVariable Long productId, @RequestBody UpdateCartItemRequest request) {
        return shoppingCartService.updateItemQuantity(customerId, productId, request);
    }

    @DeleteMapping("/{customerId}/items/{productId}")
    public CompletableFuture<ShoppingCartResponse> removeItemFromCart(@PathVariable Long customerId, @PathVariable Long productId) {
        return shoppingCartService.removeItemFromCart(customerId, productId);
    }

    @DeleteMapping("/{customerId}")
    public CompletableFuture<Void> clearShoppingCart(@PathVariable Long customerId) {
        return shoppingCartService.clearShoppingCart(customerId);
    }
}
