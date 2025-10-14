package com.ecommerce.orderprocessing.shoppingcart.service;

import com.ecommerce.orderprocessing.shoppingcart.dto.AddCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.UpdateCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.ShoppingCartResponse;

import java.util.concurrent.CompletableFuture;

public interface ShoppingCartService {
    CompletableFuture<ShoppingCartResponse> getShoppingCart(Long customerId);
    CompletableFuture<ShoppingCartResponse> addItemToCart(Long customerId, AddCartItemRequest request);
    CompletableFuture<ShoppingCartResponse> updateItemQuantity(Long customerId, Long productId, UpdateCartItemRequest request);
    CompletableFuture<ShoppingCartResponse> removeItemFromCart(Long customerId, Long productId);
    CompletableFuture<Void> clearShoppingCart(Long customerId);
}
