package com.ecommerce.orderprocessing.shoppingcart.service;

import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.shoppingcart.domain.CartItem;
import com.ecommerce.orderprocessing.shoppingcart.domain.ShoppingCart;
import com.ecommerce.orderprocessing.shoppingcart.dto.AddCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.ShoppingCartResponse;
import com.ecommerce.orderprocessing.shoppingcart.dto.UpdateCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.repository.CartItemRepository;
import com.ecommerce.orderprocessing.shoppingcart.repository.ShoppingCartRepository;
import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductCatalogService productCatalogService;

    @Mock
    private UserService userService;

    // Use a real ExecutorService for CompletableFuture testing
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @InjectMocks
    private ShoppingCartServiceImpl shoppingCartService;

    private Long customerId = 1L;
    private Long productId = 101L;
    private BigDecimal productPrice = BigDecimal.valueOf(50.00);
    private ProductResponse productResponse;
    private UserResponse userResponse;
    private ShoppingCart shoppingCart;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        // Manually inject the real ExecutorService into the service under test
        shoppingCartService = new ShoppingCartServiceImpl(
                shoppingCartRepository,
                cartItemRepository,
                productCatalogService,
                userService,
                virtualThreadExecutor
        );

        productResponse = new ProductResponse(productId, "Test Product", "Description", productPrice, true, LocalDateTime.now(), LocalDateTime.now());
        userResponse = new UserResponse(customerId, "John", "Doe", "john.doe@example.com", "123-456-7890", "http://example.com/profile.jpg", "CUSTOMER", true, LocalDateTime.now());

        shoppingCart = new ShoppingCart();
        shoppingCart.setId(1L);
        shoppingCart.setCustomerId(customerId);
        shoppingCart.setCreatedAt(LocalDateTime.now());
        shoppingCart.setUpdatedAt(LocalDateTime.now());
        shoppingCart.setCartItems(new HashSet<>());

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setShoppingCart(shoppingCart);
        cartItem.setProductId(productId);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(productPrice);
        shoppingCart.addCartItem(cartItem);
    }

    @Test
    void getShoppingCart_shouldReturnExistingCart() throws Exception {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(shoppingCart));
        when(userService.getUserProfile(customerId)).thenReturn(CompletableFuture.completedFuture(userResponse));
        when(productCatalogService.getProductById(productId)).thenReturn(CompletableFuture.completedFuture(productResponse));

        ShoppingCartResponse response = shoppingCartService.getShoppingCart(customerId).get();

        assertNotNull(response);
        assertEquals(shoppingCart.getId(), response.id());
        assertEquals(customerId, response.customerId());
        assertEquals(1, response.items().size());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verify(userService, times(1)).getUserProfile(customerId);
        verify(productCatalogService, times(1)).getProductById(productId);
    }

    @Test
    void getShoppingCart_shouldCreateNewCartIfNotFound() throws Exception {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(shoppingCart);
        when(userService.getUserProfile(customerId)).thenReturn(CompletableFuture.completedFuture(userResponse));

        ShoppingCartResponse response = shoppingCartService.getShoppingCart(customerId).get();

        assertNotNull(response);
        assertEquals(shoppingCart.getId(), response.id());
        assertEquals(customerId, response.customerId());
        assertTrue(response.items().isEmpty());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verify(shoppingCartRepository, times(1)).save(any(ShoppingCart.class));
        verify(userService, times(1)).getUserProfile(customerId);
    }

    @Test
    void addItemToCart_shouldAddNewItem() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(201L, 3);
        ProductResponse newProductResponse = new ProductResponse(201L, "New Product", "Desc", BigDecimal.valueOf(25.00), true, LocalDateTime.now(), LocalDateTime.now());

        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(shoppingCart));
        when(productCatalogService.getProductById(request.productId())).thenReturn(CompletableFuture.completedFuture(newProductResponse));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem savedItem = invocation.getArgument(0);
            savedItem.setId(2L); // Simulate save operation setting an ID
            return savedItem;
        });

        ShoppingCartResponse response = shoppingCartService.addItemToCart(customerId, request).get();

        assertNotNull(response);
        assertEquals(2, response.items().size()); // One existing, one new
        assertEquals(request.quantity(), response.items().stream().filter(item -> item.productId().equals(request.productId())).findFirst().get().quantity());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verify(productCatalogService, times(1)).getProductById(request.productId());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(userService, times(1)).getUserProfile(customerId);
    }

    @Test
    void addItemToCart_shouldIncreaseQuantityOfExistingItem() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(productId, 3);

        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(shoppingCart));
        when(productCatalogService.getProductById(request.productId())).thenReturn(CompletableFuture.completedFuture(productResponse));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        ShoppingCartResponse response = shoppingCartService.addItemToCart(customerId, request).get();

        assertNotNull(response);
        assertEquals(1, response.items().size()); // Still one item, quantity updated
        assertEquals(cartItem.getQuantity(), response.items().stream().filter(item -> item.productId().equals(productId)).findFirst().get().quantity());
        assertEquals(5, cartItem.getQuantity()); // Original 2 + new 3
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verify(productCatalogService, times(1)).getProductById(request.productId());
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(userService, times(1)).getUserProfile(customerId);
    }

    @Test
    void updateItemQuantity_shouldUpdateExistingItemQuantity() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(5);

        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(shoppingCart));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(userService.getUserProfile(customerId)).thenReturn(CompletableFuture.completedFuture(userResponse));
        when(productCatalogService.getProductById(productId)).thenReturn(CompletableFuture.completedFuture(productResponse));

        ShoppingCartResponse response = shoppingCartService.updateItemQuantity(customerId, productId, request).get();

        assertNotNull(response);
        assertEquals(1, response.items().size());
        assertEquals(request.quantity(), response.items().stream().filter(item -> item.productId().equals(productId)).findFirst().get().quantity());
        assertEquals(5, cartItem.getQuantity());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(userService, times(1)).getUserProfile(customerId);
        verify(productCatalogService, times(1)).getProductById(productId);
    }

    @Test
    void updateItemQuantity_shouldThrowExceptionIfCartNotFound() {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shoppingCartService.updateItemQuantity(customerId, productId, new UpdateCartItemRequest(1)).join());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verifyNoInteractions(cartItemRepository, productCatalogService, userService);
    }

    @Test
    void updateItemQuantity_shouldThrowExceptionIfItemNotFound() {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(shoppingCart));
        shoppingCart.getCartItems().clear(); // Ensure no items in cart

        assertThrows(ResourceNotFoundException.class, () -> shoppingCartService.updateItemQuantity(customerId, productId, new UpdateCartItemRequest(1)).join());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verifyNoInteractions(cartItemRepository, productCatalogService, userService);
    }

    @Test
    void removeItemFromCart_shouldRemoveItem() throws Exception {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(shoppingCart));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        when(userService.getUserProfile(customerId)).thenReturn(CompletableFuture.completedFuture(userResponse));

        ShoppingCartResponse response = shoppingCartService.removeItemFromCart(customerId, productId).get();

        assertNotNull(response);
        assertTrue(response.items().isEmpty());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(userService, times(1)).getUserProfile(customerId);
    }

    @Test
    void removeItemFromCart_shouldThrowExceptionIfCartNotFound() {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shoppingCartService.removeItemFromCart(customerId, productId).join());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verifyNoInteractions(cartItemRepository, productCatalogService, userService);
    }

    @Test
    void removeItemFromCart_shouldThrowExceptionIfItemNotFound() {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(shoppingCart));
        shoppingCart.getCartItems().clear(); // Ensure no items in cart

        assertThrows(ResourceNotFoundException.class, () -> shoppingCartService.removeItemFromCart(customerId, productId).join());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verifyNoInteractions(cartItemRepository, productCatalogService, userService);
    }

    @Test
    void clearShoppingCart_shouldClearAllItems() throws Exception {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.of(shoppingCart));
        doNothing().when(cartItemRepository).deleteAll(anyCollection());
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenReturn(shoppingCart);

        shoppingCartService.clearShoppingCart(customerId).get();

        assertTrue(shoppingCart.getCartItems().isEmpty());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verify(cartItemRepository, times(1)).deleteAll(anyCollection());
        verify(shoppingCartRepository, times(1)).save(shoppingCart);
    }

    @Test
    void clearShoppingCart_shouldThrowExceptionIfCartNotFound() {
        when(shoppingCartRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shoppingCartService.clearShoppingCart(customerId).join());
        verify(shoppingCartRepository, times(1)).findByCustomerId(customerId);
        verifyNoInteractions(cartItemRepository, productCatalogService, userService);
    }
}