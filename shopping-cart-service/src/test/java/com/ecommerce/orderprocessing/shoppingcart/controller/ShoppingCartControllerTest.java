package com.ecommerce.orderprocessing.shoppingcart.controller;

import com.ecommerce.orderprocessing.order.controller.OrderController;
import com.ecommerce.orderprocessing.order.service.OrderService;
import com.ecommerce.orderprocessing.product.controller.ProductController;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import com.ecommerce.orderprocessing.shoppingcart.dto.AddCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.CartItemResponse;
import com.ecommerce.orderprocessing.shoppingcart.dto.ShoppingCartResponse;
import com.ecommerce.orderprocessing.shoppingcart.dto.UpdateCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.service.ShoppingCartService;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ShoppingCartController.class)
@Import({ShoppingCartModelAssembler.class, ProductController.class, OrderController.class})
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShoppingCartService shoppingCartService;

    @MockitoBean
    private ProductCatalogService productCatalogService; // For ProductController links

    @MockitoBean
    private OrderService orderService; // For OrderController links

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final Long currentUserId = 1L;
    private final Long otherUserId = 2L;

    @BeforeEach
    void setUp() {
        // Default to being authenticated as the CUSTOMER with ID 1
        setupAsCustomer(currentUserId);
    }

    private void setupAsCustomer(Long customerId) {
        AppUserDetails userDetails = new AppUserDetails(customerId, "customer@example.com", "password", "CUSTOMER", true, Collections.emptyMap());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getShoppingCart_forOwnCart_shouldReturnCartWithLinks() throws Exception {
        CartItemResponse item = new CartItemResponse(101L, "Test Product", 2, BigDecimal.TEN, BigDecimal.valueOf(20));
        ShoppingCartResponse shoppingCartResponse = new ShoppingCartResponse(1L, currentUserId, "Test Customer", "test@example.com", List.of(item), BigDecimal.valueOf(20), LocalDateTime.now(), LocalDateTime.now());

        when(shoppingCartService.getShoppingCart(currentUserId)).thenReturn(CompletableFuture.completedFuture(shoppingCartResponse));

        mockMvc.perform(get("/api/cart/{customerId}", currentUserId).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerId", is(currentUserId.intValue())))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/cart/" + currentUserId)))
                .andExpect(jsonPath("$._links.checkout.href", endsWith("/api/orders")))
                .andExpect(jsonPath("$._links.clear.href", endsWith("/api/cart/" + currentUserId)))
                .andExpect(jsonPath("$._links.add-item.href", endsWith("/api/cart/" + currentUserId + "/items")))
                .andExpect(jsonPath("$._links.item-101-product.href", endsWith("/api/products/101")));
    }

    @Test
    void getShoppingCart_forOtherUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/cart/{customerId}", otherUserId).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isForbidden());
    }

    @Test
    void getShoppingCart_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/cart/{customerId}", currentUserId).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addItemToCart_forOwnCart_shouldReturnUpdatedCart() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(101L, 2);
        ShoppingCartResponse shoppingCartResponse = new ShoppingCartResponse(1L, currentUserId, "Test Customer", "test@example.com", Collections.emptyList(), BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());
        when(shoppingCartService.addItemToCart(eq(currentUserId), any(AddCartItemRequest.class))).thenReturn(CompletableFuture.completedFuture(shoppingCartResponse));

        mockMvc.perform(post("/api/cart/{customerId}/items", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/cart/" + currentUserId)));
    }

    @Test
    void addItemToCart_forOtherUser_shouldReturnForbidden() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest(101L, 2);
        mockMvc.perform(post("/api/cart/{customerId}/items", otherUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateItemQuantity_forOwnCart_shouldReturnUpdatedCart() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        ShoppingCartResponse shoppingCartResponse = new ShoppingCartResponse(1L, currentUserId, "Test Customer", "test@example.com", Collections.emptyList(), BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());
        when(shoppingCartService.updateItemQuantity(eq(currentUserId), eq(101L), any(UpdateCartItemRequest.class))).thenReturn(CompletableFuture.completedFuture(shoppingCartResponse));

        mockMvc.perform(put("/api/cart/{customerId}/items/{productId}", currentUserId, 101L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/cart/" + currentUserId)));
    }

    @Test
    void removeItemFromCart_forOwnCart_shouldReturnUpdatedCart() throws Exception {
        ShoppingCartResponse shoppingCartResponse = new ShoppingCartResponse(1L, currentUserId, "Test Customer", "test@example.com", Collections.emptyList(), BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());
        when(shoppingCartService.removeItemFromCart(currentUserId, 101L)).thenReturn(CompletableFuture.completedFuture(shoppingCartResponse));

        mockMvc.perform(delete("/api/cart/{customerId}/items/{productId}", currentUserId, 101L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/cart/" + currentUserId)));
    }

    @Test
    void clearShoppingCart_forOwnCart_shouldReturnOk() throws Exception {
        when(shoppingCartService.clearShoppingCart(currentUserId)).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/api/cart/{customerId}", currentUserId))
                .andExpect(status().isOk());
    }

    @Test
    void clearShoppingCart_forOtherUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/cart/{customerId}", otherUserId))
                .andExpect(status().isForbidden());
    }
}