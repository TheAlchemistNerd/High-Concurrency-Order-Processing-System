package com.ecommerce.orderprocessing.order.controller;

import com.ecommerce.orderprocessing.common.dto.PagedResponse;
import com.ecommerce.orderprocessing.order.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.order.dto.OrderItemResponse;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.order.service.OrderService;
import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import com.ecommerce.orderprocessing.product.controller.ProductController;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import com.ecommerce.orderprocessing.user.controller.UserController;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import com.ecommerce.orderprocessing.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
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
@WebMvcTest(OrderController.class)
@Import({OrderModelAssembler.class, PaymentModelAssembler.class, ProductController.class, UserController.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    // Mocks for cross-module link building
    @MockBean
    private ProductCatalogService productCatalogService;
    @MockBean
    private UserService userService;

    // Mocks for security
    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private OrderResponse pendingOrder;
    private OrderResponse cancelledOrder;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        OrderItemResponse item = new OrderItemResponse(1L, 101L, "Test Product", 1, BigDecimal.TEN, BigDecimal.TEN);
        pendingOrder = new OrderResponse(1L, 1L, "Customer", "customer@example.com", "PENDING_PAYMENT", BigDecimal.TEN, "123 Main St", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), List.of(item));
        cancelledOrder = new OrderResponse(2L, 1L, "Customer", "customer@example.com", "CANCELLED", BigDecimal.TEN, "123 Main St", null, "Notes", LocalDateTime.now(), LocalDateTime.now(), List.of(item));
    }

    private void setupAs(String role, Long userId) {
        AppUserDetails userDetails = new AppUserDetails(userId, "user@example.com", "password", role, true, Collections.emptyMap());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createOrder_asCustomer_shouldReturnCreatedOrderWithLinks() throws Exception {
        setupAs("CUSTOMER", 1L);
        CreateOrderRequest request = new CreateOrderRequest(1L, "Address", Collections.emptyList(), "Notes");
        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(CompletableFuture.completedFuture(pendingOrder));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/orders/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/orders/1")));
    }

    @Test
    void getOrder_asCustomerForOwnOrder_shouldReturnOrderWithLinks() throws Exception {
        setupAs("CUSTOMER", 1L);
        when(orderService.getOrder(1L)).thenReturn(CompletableFuture.completedFuture(pendingOrder));

        mockMvc.perform(get("/api/orders/{orderId}", 1L).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/orders/1")))
                .andExpect(jsonPath("$._links.customer.href", endsWith("/api/users/1")))
                .andExpect(jsonPath("$._links.payment.href", endsWith("/api/orders/payment")))
                .andExpect(jsonPath("$._links.cancel.href", endsWith("/api/orders/1/cancel")))
                .andExpect(jsonPath("$._links.item-101-product.href", endsWith("/api/products/101")));
    }

    @Test
    void getOrder_asAdmin_shouldReturnOrder() throws Exception {
        setupAs("ADMIN", 99L);
        when(orderService.getOrder(1L)).thenReturn(CompletableFuture.completedFuture(pendingOrder));

        mockMvc.perform(get("/api/orders/{orderId}", 1L).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void getCustomerOrders_asCustomerForOwnOrders_shouldReturnPagedOrders() throws Exception {
        setupAs("CUSTOMER", 1L);
        PagedResponse<OrderResponse> pagedResponse = new PagedResponse<>(List.of(pendingOrder), 1, 0, 1, true, true);
        when(orderService.getCustomerOrders(eq(1L), any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(pagedResponse));

        mockMvc.perform(get("/api/orders/customer/{customerId}", 1L).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.orderResponseList[0].id", is(1)))
                .andExpect(jsonPath("$._embedded.orderResponseList[0]._links.self.href", endsWith("/api/orders/1")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/orders/customer/1")));
    }

    @Test
    void getCustomerOrders_asCustomerForOtherOrders_shouldReturnForbidden() throws Exception {
        setupAs("CUSTOMER", 1L);
        mockMvc.perform(get("/api/orders/customer/{customerId}", 2L).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderStatus_asOrderManager_shouldReturnUpdatedOrder() throws Exception {
        setupAs("ORDER_MANAGER", 99L);
        when(orderService.updateOrderStatus(eq(1L), any())).thenReturn(CompletableFuture.completedFuture(cancelledOrder));

        mockMvc.perform(put("/api/orders/{orderId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/orders/2")));
    }

    @Test
    void updateOrderStatus_asCustomer_shouldReturnForbidden() throws Exception {
        setupAs("CUSTOMER", 1L);
        mockMvc.perform(put("/api/orders/{orderId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void processOrderPayment_asCustomer_shouldReturnPaymentWithLinks() throws Exception {
        setupAs("CUSTOMER", 1L);
        PaymentRequest paymentRequest = new PaymentRequest(1L, BigDecimal.TEN, "USD", "tok_visa");
        PaymentResponse paymentResponse = new PaymentResponse("txn_123", 1L, "succeeded", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "ch_123", "Success");
        when(orderService.processOrderPayment(any(PaymentRequest.class))).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        mockMvc.perform(post("/api/orders/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is("txn_123")))
                .andExpect(jsonPath("$._links.order.href", endsWith("/api/orders/1")));
    }

    @Test
    void getAllOrders_asAdmin_shouldReturnPagedOrders() throws Exception {
        setupAs("ADMIN", 99L);
        PagedResponse<OrderResponse> pagedResponse = new PagedResponse<>(List.of(pendingOrder, cancelledOrder), 2, 0, 2, true, true);
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(CompletableFuture.completedFuture(pagedResponse));

        mockMvc.perform(get("/api/orders").accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.orderResponseList.length()", is(2)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/orders")));
    }

    @Test
    void getAllOrders_asCustomer_shouldReturnForbidden() throws Exception {
        setupAs("CUSTOMER", 1L);
        mockMvc.perform(get("/api/orders").accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isForbidden());
    }
}