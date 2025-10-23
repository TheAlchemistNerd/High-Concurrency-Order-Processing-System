package com.ecommerce.orderprocessing.order.controller;

import com.ecommerce.orderprocessing.order.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.order.service.OrderService;
import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
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

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private AppUserDetails customerUserDetails;
    private AppUserDetails adminUserDetails;
    private AppUserDetails orderManagerUserDetails;
    private AppUserDetails supportUserDetails;

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        customerUserDetails = new AppUserDetails(1L, "customer@example.com", "password", "CUSTOMER", true, Collections.emptyMap());
        adminUserDetails = new AppUserDetails(2L, "admin@example.com", "password", "ADMIN", true, Collections.emptyMap());
        orderManagerUserDetails = new AppUserDetails(3L, "ordermanager@example.com", "password", "ORDER_MANAGER", true, Collections.emptyMap());
        supportUserDetails = new AppUserDetails(4L, "support@example.com", "password", "SUPPORT", true, Collections.emptyMap());

        // Default authentication for tests (e.g., customer)
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        ));

        orderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "PENDING", BigDecimal.ZERO, "Address", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
    }

    @Test
    void createOrder_shouldReturnOrder() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest(1L, "Address", Collections.emptyList(), "Notes");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(CompletableFuture.completedFuture(orderResponse));

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void createOrder_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        CreateOrderRequest request = new CreateOrderRequest(1L, "Address", Collections.emptyList(), "Notes");

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOrder_shouldReturnOrder() throws Exception {
        // Given
        when(orderService.getOrder(1L)).thenReturn(CompletableFuture.completedFuture(orderResponse));

        // When & Then
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void getOrder_withAdminRole_shouldReturnOrder() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        when(orderService.getOrder(1L)).thenReturn(CompletableFuture.completedFuture(orderResponse));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrder_withOrderManagerRole_shouldReturnOrder() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                orderManagerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORDER_MANAGER"))
        ));
        when(orderService.getOrder(1L)).thenReturn(CompletableFuture.completedFuture(orderResponse));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrder_withSupportRole_shouldReturnOrder() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                supportUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORT"))
        ));
        when(orderService.getOrder(1L)).thenReturn(CompletableFuture.completedFuture(orderResponse));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrder_withCustomerRole_forOwnOrder_shouldReturnOrder() throws Exception {
        when(orderService.getOrder(1L)).thenReturn(CompletableFuture.completedFuture(orderResponse));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getOrder_withCustomerRole_forOtherOrder_shouldReturnForbidden() throws Exception {
        when(orderService.getOrder(2L)).thenReturn(CompletableFuture.completedFuture(orderResponse));

        mockMvc.perform(get("/api/orders/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrder_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCustomerOrders_withAdminRole_shouldReturnOrders() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        when(orderService.getCustomerOrders(anyLong(), any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(new PagedResponse<>(Collections.singletonList(orderResponse), 1, 1, 1L, true, true)));

        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCustomerOrders_withOrderManagerRole_shouldReturnOrders() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                orderManagerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORDER_MANAGER"))
        ));
        when(orderService.getCustomerOrders(anyLong(), any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(new PagedResponse<>(Collections.singletonList(orderResponse), 1, 1, 1L, true, true)));

        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCustomerOrders_withSupportRole_shouldReturnOrders() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                supportUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORT"))
        ));
        when(orderService.getCustomerOrders(anyLong(), any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(new PagedResponse<>(Collections.singletonList(orderResponse), 1, 1, 1L, true, true)));

        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCustomerOrders_withCustomerRole_forOwnOrders_shouldReturnOrders() throws Exception {
        when(orderService.getCustomerOrders(anyLong(), any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(new PagedResponse<>(Collections.singletonList(orderResponse), 1, 1, 1L, true, true)));

        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getCustomerOrders_withCustomerRole_forOtherOrders_shouldReturnForbidden() throws Exception {
        when(orderService.getCustomerOrders(anyLong(), any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(new PagedResponse<>(Collections.singletonList(orderResponse), 1, 1, 1L, true, true)));

        mockMvc.perform(get("/api/orders/customer/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCustomerOrders_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateOrderStatus_shouldReturnOrder() throws Exception {
        // Given
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PAID", "Notes");
        OrderResponse updatedOrderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "PAID", BigDecimal.ZERO, "Address", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.updateOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class))).thenReturn(CompletableFuture.completedFuture(updatedOrderResponse));

        // When & Then
        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderStatus_withAdminRole_shouldReturnOrder() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PAID", "Notes");
        OrderResponse updatedOrderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "PAID", BigDecimal.ZERO, "Address", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.updateOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class))).thenReturn(CompletableFuture.completedFuture(updatedOrderResponse));

        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderStatus_withOrderManagerRole_shouldReturnOrder() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                orderManagerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORDER_MANAGER"))
        ));
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PAID", "Notes");
        OrderResponse updatedOrderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "PAID", BigDecimal.ZERO, "Address", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.updateOrderStatus(eq(1L), any(UpdateOrderStatusRequest.class))).thenReturn(CompletableFuture.completedFuture(updatedOrderResponse));

        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateOrderStatus_withCustomerRole_shouldReturnForbidden() throws Exception {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PAID", "Notes");

        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderStatus_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest("PAID", "Notes");

        mockMvc.perform(put("/api/orders/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void processOrderPayment_withCustomerRole_shouldReturnPaymentResponse() throws Exception {
        PaymentRequest paymentRequest = new PaymentRequest(1L, BigDecimal.valueOf(100.00), "USD", "token");
        PaymentResponse paymentResponse = new PaymentResponse("success", "txn123");
        when(orderService.processOrderPayment(any(PaymentRequest.class))).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        mockMvc.perform(post("/api/orders/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void processOrderPayment_withAdminRole_shouldReturnForbidden() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        PaymentRequest paymentRequest = new PaymentRequest(1L, BigDecimal.valueOf(100.00), "USD", "token");

        mockMvc.perform(post("/api/orders/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void processOrderPayment_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        PaymentRequest paymentRequest = new PaymentRequest(1L, BigDecimal.valueOf(100.00), "USD", "token");

        mockMvc.perform(post("/api/orders/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cancelOrder_shouldReturnOrder() throws Exception {
        // Given
        OrderResponse cancelledOrderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "CANCELLED", BigDecimal.ZERO, "Address", null, "Reason", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.cancelOrder(1L, "Reason")).thenReturn(CompletableFuture.completedFuture(cancelledOrderResponse));

        // When & Then
        mockMvc.perform(put("/api/orders/1/cancel?reason=Reason"))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_withAdminRole_shouldReturnOrder() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        OrderResponse cancelledOrderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "CANCELLED", BigDecimal.ZERO, "Address", null, "Reason", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.cancelOrder(1L, "Reason")).thenReturn(CompletableFuture.completedFuture(cancelledOrderResponse));

        mockMvc.perform(put("/api/orders/1/cancel?reason=Reason"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_withOrderManagerRole_shouldReturnOrder() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                orderManagerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORDER_MANAGER"))
        ));
        OrderResponse cancelledOrderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "CANCELLED", BigDecimal.ZERO, "Address", null, "Reason", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.cancelOrder(1L, "Reason")).thenReturn(CompletableFuture.completedFuture(cancelledOrderResponse));

        mockMvc.perform(put("/api/orders/1/cancel?reason=Reason"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_withCustomerRole_forOwnOrder_shouldReturnOrder() throws Exception {
        OrderResponse cancelledOrderResponse = new OrderResponse(1L, 1L, "Test Customer", "test@test.com", "CANCELLED", BigDecimal.ZERO, "Address", null, "Reason", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.cancelOrder(1L, "Reason")).thenReturn(CompletableFuture.completedFuture(cancelledOrderResponse));

        mockMvc.perform(put("/api/orders/1/cancel?reason=Reason"))
                .andExpect(status().isOk());
    }

    @Test
    void cancelOrder_withCustomerRole_forOtherOrder_shouldReturnForbidden() throws Exception {
        OrderResponse cancelledOrderResponse = new OrderResponse(2L, 2L, "Other Customer", "other@test.com", "CANCELLED", BigDecimal.ZERO, "Address", null, "Reason", LocalDateTime.now(), LocalDateTime.now(), Collections.emptyList());
        when(orderService.cancelOrder(2L, "Reason")).thenReturn(CompletableFuture.completedFuture(cancelledOrderResponse));

        mockMvc.perform(put("/api/orders/2/cancel?reason=Reason"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelOrder_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(put("/api/orders/1/cancel?reason=Reason"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllOrders_withAdminRole_shouldReturnOrders() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ));
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(new PagedResponse<>(Collections.singletonList(orderResponse), 1, 1, 1L, true, true)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrders_withOrderManagerRole_shouldReturnOrders() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                orderManagerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ORDER_MANAGER"))
        ));
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(new PagedResponse<>(Collections.singletonList(orderResponse), 1, 1, 1L, true, true)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrders_withSupportRole_shouldReturnOrders() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                supportUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPPORT"))
        ));
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(new PagedResponse<>(Collections.singletonList(orderResponse), 1, 1, 1L, true, true)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllOrders_withCustomerRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrders_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }
}


