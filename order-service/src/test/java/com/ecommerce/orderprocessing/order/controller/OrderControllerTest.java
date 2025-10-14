package com.ecommerce.orderprocessing.order.controller;

import com.ecommerce.orderprocessing.order.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createOrder_shouldReturnOrder() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(1L, "Address", Collections.emptyList(), "Notes");
        OrderResponse orderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "PENDING", BigDecimal.ZERO, "Address", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(CompletableFuture.completedFuture(orderResponse));

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"customerId\": 1, \"shippingAddress\": \"Address\", \"orderItems\": [], \"notes\": \"Notes\" }"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void getOrder_shouldReturnOrder() throws Exception {
        // Given
        OrderResponse orderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "PENDING", BigDecimal.ZERO, "Address", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.getOrder(1L)).thenReturn(CompletableFuture.completedFuture(orderResponse));

        // When & Then
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderStatus_shouldReturnOrder() throws Exception {
        // Given
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PAID", "Notes");
        OrderResponse orderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "PAID", BigDecimal.ZERO, "Address", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.updateOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class))).thenReturn(CompletableFuture.completedFuture(orderResponse));

        // When & Then
        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"status\": \"PAID\", \"notes\": \"Notes\" }"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_shouldReturnOrder() throws Exception {
        // Given
        OrderResponse orderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "CANCELLED", BigDecimal.ZERO, "Address", null, "Reason", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.cancelOrder(1L, "Reason")).thenReturn(CompletableFuture.completedFuture(orderResponse));

        // When & Then
        mockMvc.perform(put("/api/orders/1/cancel?reason=Reason"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }
}

