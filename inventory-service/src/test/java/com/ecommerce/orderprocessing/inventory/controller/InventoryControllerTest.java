package com.ecommerce.orderprocessing.inventory.controller;

import com.ecommerce.orderprocessing.inventory.dto.InventoryResponse;
import com.ecommerce.orderprocessing.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

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

    @Test
    void reserveInventory_shouldReturnOk() throws Exception {
        // Given
        when(inventoryService.reserveInventory(1L, 5)).thenReturn(CompletableFuture.completedFuture(null));

        // When & Then
        mockMvc.perform(post("/api/inventory/products/1/reserve?quantity=5"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
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
