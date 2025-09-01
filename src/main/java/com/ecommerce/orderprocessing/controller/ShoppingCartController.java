package com.ecommerce.orderprocessing.controller;

import com.ecommerce.orderprocessing.dto.request.AddItemToCartRequest;
import com.ecommerce.orderprocessing.dto.request.UpdateCartItemQuantityRequest;
import com.ecommerce.orderprocessing.dto.response.ShoppingCartDto;
import com.ecommerce.orderprocessing.service.ShoppingCartService;
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
    public CompletableFuture<ShoppingCartDto> getShoppingCart(@PathVariable Long customerId) {
        return shoppingCartService.getShoppingCart(customerId);
    }

    @PostMapping("/{customerId}/items")
    public CompletableFuture<ShoppingCartDto> addItemToCart(@PathVariable Long customerId, @RequestBody AddItemToCartRequest request) {
        return shoppingCartService.addItemToCart(customerId, request);
    }

    @PutMapping("/{customerId}/items/{productId}")
    public CompletableFuture<ShoppingCartDto> updateItemQuantity(@PathVariable Long customerId, @PathVariable Long productId, @RequestBody UpdateCartItemQuantityRequest request) {
        return shoppingCartService.updateItemQuantity(customerId, productId, request);
    }

    @DeleteMapping("/{customerId}/items/{productId}")
    public CompletableFuture<ShoppingCartDto> removeItemFromCart(@PathVariable Long customerId, @PathVariable Long productId) {
        return shoppingCartService.removeItemFromCart(customerId, productId);
    }

    @DeleteMapping("/{customerId}")
    public CompletableFuture<Void> clearShoppingCart(@PathVariable Long customerId) {
        return shoppingCartService.clearShoppingCart(customerId);
    }
}
