package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.dto.request.AddItemToCartRequest;
import com.ecommerce.orderprocessing.dto.request.UpdateCartItemQuantityRequest;
import com.ecommerce.orderprocessing.dto.response.ShoppingCartDto;

import java.util.concurrent.CompletableFuture;

public interface ShoppingCartService {
    CompletableFuture<ShoppingCartDto> getShoppingCart(Long customerId);
    CompletableFuture<ShoppingCartDto> addItemToCart(Long customerId, AddItemToCartRequest request);
    CompletableFuture<ShoppingCartDto> updateItemQuantity(Long customerId, Long productId, UpdateCartItemQuantityRequest request);
    CompletableFuture<ShoppingCartDto> removeItemFromCart(Long customerId, Long productId);
    CompletableFuture<Void> clearShoppingCart(Long customerId);
}
