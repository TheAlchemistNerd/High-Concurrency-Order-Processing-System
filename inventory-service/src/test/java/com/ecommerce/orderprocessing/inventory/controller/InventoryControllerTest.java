package com.ecommerce.orderprocessing.inventory.controller;

import com.ecommerce.orderprocessing.inventory.dto.InventoryResponse;
import com.ecommerce.orderprocessing.inventory.service.InventoryService;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        AppUserDetails orderManagerUserDetails = new AppUserDetails(1L, "manager@example.com", "password", "ORDER_MANAGER", true, Collections.emptyMap());
        Authentication orderManagerAuthentication = new UsernamePasswordAuthenticationToken(
                orderManagerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORDER_MANAGER"))
        );
        SecurityContextHolder.getContext().setAuthentication(orderManagerAuthentication);
    }

    @Test
    void checkInventory_shouldReturnBoolean() throws Exception {
        // Given
        when(inventoryService.checkInventory(1L, 5)).thenReturn(CompletableFuture.completedFuture(true));

        // When & Then
        mockMvc.perform(get("/api/inventory/products/1/check?quantity=5"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isBoolean())
                .andExpect(jsonPath("$").value(true));
    }

    void reserveInventory_withOrderManagerRole_shouldReturnOk() throws Exception {
        // Given
        when(inventoryService.reserveInventory(1L, 5)).thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(post("/api/inventory/products/1/reserve?quantity=5"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void reserveInventory_withCustomerRole_shouldReturnForbidden() throws Exception {
        AppUserDetails customerUserDetails = new AppUserDetails(2L, "customer@example.com", "password", "CUSTOMER", true, Collections.emptyMap());
        Authentication customerAuthentication = new UsernamePasswordAuthenticationToken(
                customerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(customerAuthentication);

        mockMvc.perform(post("/api/inventory/products/1/reserve?quantity=5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void reserveInventory_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(post("/api/inventory/products/1/reserve?quantity=5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void restockInventory_withAdminRole_shouldReturnOk() throws Exception {
        AppUserDetails adminUserDetails = new AppUserDetails(3L, "admin@example.com", "password", "ADMIN", true, Collections.emptyMap());
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuthentication);

        when(inventoryService.restockInventory(1L, 10)).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/inventory/products/1/restock?quantity=10"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void restockInventory_withOrderManagerRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/inventory/products/1/restock?quantity=10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void releaseInventory_shouldReturnOk() throws Exception { // Renamed from restoreInventory
        // Given
        when(inventoryService.releaseInventory(1L, 5)).thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(post("/api/inventory/products/1/release?quantity=5")) // Updated URL
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void commitInventory_shouldReturnOk() throws Exception {
        // Given
        when(inventoryService.commitInventory(1L, 5)).thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(post("/api/inventory/products/1/commit?quantity=5"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void getInventoryByProductId_shouldReturnInventoryResponse() throws Exception { // Renamed
        // Given
        InventoryResponse inventoryResponse = new InventoryResponse(1L, 10);
        when(inventoryService.getInventoryByProductId(1L)).thenReturn(CompletableFuture.completedFuture(inventoryResponse));

        // When & Then
        mockMvc.perform(get("/api/inventory/products/1")) // Updated URL
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1L))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }
}
