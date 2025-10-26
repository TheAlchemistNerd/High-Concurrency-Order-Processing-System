package com.ecommerce.orderprocessing.shoppingcart.service;

import com.ecommerce.orderprocessing.shoppingcart.domain.CartItem;
import com.ecommerce.orderprocessing.shoppingcart.domain.ShoppingCart;
import com.ecommerce.orderprocessing.shoppingcart.dto.AddCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.CartItemResponse;
import com.ecommerce.orderprocessing.shoppingcart.dto.ShoppingCartResponse;
import com.ecommerce.orderprocessing.shoppingcart.dto.UpdateCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.repository.CartItemRepository;
import com.ecommerce.orderprocessing.shoppingcart.repository.ShoppingCartRepository;
import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.user.service.UserService;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductCatalogService productCatalogService;
    private final UserService userService;
    private final ExecutorService virtualThreadExecutor;

    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository,
                                   CartItemRepository cartItemRepository,
                                   ProductCatalogService productCatalogService,
                                   UserService userService,
                                   ExecutorService virtualThreadExecutor) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productCatalogService = productCatalogService;
        this.userService = userService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    public CompletableFuture<ShoppingCartResponse> getShoppingCart(Long customerId) {
        return CompletableFuture.supplyAsync(() -> {
            ShoppingCart shoppingCart = shoppingCartRepository.findByCustomerId(customerId)
                    .orElseGet(() -> createNewShoppingCart(customerId));
            return toShoppingCartResponse(shoppingCart, customerId);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<ShoppingCartResponse> addItemToCart(Long customerId, AddCartItemRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                    .orElseGet(() -> createNewShoppingCart(customerId));

            ProductResponse productResponse = productCatalogService.getProductById(request.productId()).join();

            // Check if item already exists in cart
            cart.getCartItems().stream()
                    .filter(item -> item.getProductId().equals(productResponse.id()))
                    .findFirst()
                    .ifPresentOrElse(
                            item -> {
                                item.setQuantity(item.getQuantity() + request.quantity());
                                cartItemRepository.save(item);
                            },
                            () -> {
                                CartItem newItem = new CartItem();
                                newItem.setShoppingCart(cart);
                                newItem.setProductId(productResponse.id());
                                newItem.setQuantity(request.quantity());
                                newItem.setUnitPrice(productResponse.price());
                                cartItemRepository.save(newItem);
                                cart.addCartItem(newItem);
                            }
                    );

            return toShoppingCartResponse(cart, customerId);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<ShoppingCartResponse> updateItemQuantity(Long customerId, Long productId, UpdateCartItemRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));
            CartItem itemToUpdate = cart.getCartItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

            itemToUpdate.setQuantity(request.quantity());
            cartItemRepository.save(itemToUpdate);

            return toShoppingCartResponse(cart, customerId);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<ShoppingCartResponse> removeItemFromCart(Long customerId, Long productId) {
        return CompletableFuture.supplyAsync(() -> {
            ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

            CartItem itemToRemove = cart.getCartItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

            cart.removeCartItem(itemToRemove);
            cartItemRepository.delete(itemToRemove);

            return toShoppingCartResponse(cart, customerId);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<Void> clearShoppingCart(Long customerId) {
        return CompletableFuture.runAsync(() -> {
            ShoppingCart cart = shoppingCartRepository.findByCustomerId(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Shopping cart not found"));

            cartItemRepository.deleteAll(cart.getCartItems());
            cart.getCartItems().clear();
            shoppingCartRepository.save(cart);
        }, virtualThreadExecutor);
    }

    private ShoppingCart createNewShoppingCart(Long customerId) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCustomerId(customerId);
        return shoppingCartRepository.save(shoppingCart);
    }

    private ShoppingCartResponse toShoppingCartResponse(ShoppingCart cart, Long customerId) {
        UserResponse userResponse = userService.getUserProfile(customerId).join();

        List<CartItemResponse> itemDtos = cart.getCartItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemDtos.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShoppingCartResponse(
                cart.getId(),
                userResponse.id(),
                userResponse.firstName() + " " + userResponse.lastName(),
                userResponse.email(),
                itemDtos,
                totalAmount,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        ProductResponse productResponse = productCatalogService.getProductById(item.getProductId()).join();
        return new CartItemResponse(
                productResponse.id(),
                productResponse.name(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}