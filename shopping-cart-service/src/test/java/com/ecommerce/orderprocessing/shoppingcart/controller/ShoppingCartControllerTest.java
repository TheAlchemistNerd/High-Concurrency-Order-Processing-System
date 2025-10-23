package com.ecommerce.orderprocessing.shoppingcart.controller;

import com.ecommerce.orderprocessing.shoppingcart.dto.AddCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.dto.ShoppingCartResponse;
import com.ecommerce.orderprocessing.shoppingcart.dto.UpdateCartItemRequest;
import com.ecommerce.orderprocessing.shoppingcart.service.ShoppingCartService;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ShoppingCartController.class)
class ShoppingCartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingCartService shoppingCartService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private AppUserDetails appUserDetails;

    @BeforeEach
    void setUp() {
        appUserDetails = new AppUserDetails(1L, "john.doe@example.com", "password", "CUSTOMER", true, Collections.emptyMap());

        // Set up security context for authenticated user
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                appUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getShoppingCart_shouldReturnShoppingCart() throws Exception {
        // Given
        Long customerId = 1L;
        ShoppingCartResponse shoppingCartResponse = new ShoppingCartResponse(1L, customerId, "Test Customer", "test@example.com", Collections.emptyList(), BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());

        when(shoppingCartService.getShoppingCart(customerId)).thenReturn(CompletableFuture.completedFuture(shoppingCartResponse));

        // When & Then
        mockMvc.perform(get("/api/cart/{customerId}", customerId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void getShoppingCart_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/cart/{customerId}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getShoppingCart_differentUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/cart/{customerId}", 2L))
                .andExpect(status().isForbidden());
    }

    @Test
    void addItemToCart_shouldReturnUpdatedCart() throws Exception {
        // Given
        Long customerId = 1L;
        AddCartItemRequest request = new AddCartItemRequest(1L, 2);
        ShoppingCartResponse shoppingCartResponse = new ShoppingCartResponse(1L, customerId, "Test Customer", "test@example.com", Collections.emptyList(), BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());

        when(shoppingCartService.addItemToCart(eq(customerId), any(AddCartItemRequest.class))).thenReturn(CompletableFuture.completedFuture(shoppingCartResponse));

        // When & Then
        mockMvc.perform(post("/api/cart/{customerId}/items", customerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"productId\": 1, \"quantity\": 2 }"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void updateItemQuantity_shouldReturnUpdatedCart() throws Exception {
        // Given
        Long customerId = 1L;
        Long productId = 1L;
        UpdateCartItemRequest request = new UpdateCartItemRequest(3);
        ShoppingCartResponse shoppingCartResponse = new ShoppingCartResponse(1L, customerId, "Test Customer", "test@example.com", Collections.emptyList(), BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());

        when(shoppingCartService.updateItemQuantity(eq(customerId), eq(productId), any(UpdateCartItemRequest.class))).thenReturn(CompletableFuture.completedFuture(shoppingCartResponse));

        // When & Then
        mockMvc.perform(put("/api/cart/{customerId}/items/{productId}", customerId, productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"quantity\": 3 }"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void removeItemFromCart_shouldReturnUpdatedCart() throws Exception {
        // Given
        Long customerId = 1L;
        Long productId = 1L;
        ShoppingCartResponse shoppingCartResponse = new ShoppingCartResponse(1L, customerId, "Test Customer", "test@example.com", Collections.emptyList(), BigDecimal.ZERO, LocalDateTime.now(), LocalDateTime.now());

        when(shoppingCartService.removeItemFromCart(customerId, productId)).thenReturn(CompletableFuture.completedFuture(shoppingCartResponse));

        // When & Then
        mockMvc.perform(delete("/api/cart/{customerId}/items/{productId}", customerId, productId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void clearShoppingCart_shouldReturnNoContent() throws Exception {
        // Given
        Long customerId = 1L;
        when(shoppingCartService.clearShoppingCart(customerId)).thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(delete("/api/cart/{customerId}", customerId))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }
}
