package com.ecommerce.orderprocessing.order.service;

import com.ecommerce.orderprocessing.order.domain.entity.Order;
import com.ecommerce.orderprocessing.order.domain.entity.OrderItem;
import com.ecommerce.orderprocessing.order.domain.enumeration.OrderStatus;
import com.ecommerce.orderprocessing.order.dto.CreateOrderItemRequest;
import com.ecommerce.orderprocessing.order.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.order.repository.OrderItemRepository;
import com.ecommerce.orderprocessing.order.repository.OrderRepository;
import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import com.ecommerce.orderprocessing.payment.exception.PaymentProcessingException;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.common.dto.PagedResponse;
import com.ecommerce.orderprocessing.order.exception.InvalidOrderStateException;
import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.inventory.service.InventoryService;
import com.ecommerce.orderprocessing.payment.service.PaymentService;
import com.ecommerce.orderprocessing.user.service.UserService;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ProductCatalogService productCatalogService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private UserService userService;

    private ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private OrderServiceImpl orderServiceImpl;

    @BeforeEach
    void setUp() {
        orderServiceImpl = new OrderServiceImpl(orderRepository, orderItemRepository, productCatalogService, inventoryService, paymentService, userService, virtualThreadExecutor);
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() throws Exception {
        // Given
        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest(1L, 2);
        CreateOrderRequest orderRequest = new CreateOrderRequest(1L, "123 Main St", Collections.singletonList(itemRequest), "notes");

        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());
        ProductResponse productResponse = new ProductResponse(1L, "Test Product", "Description", BigDecimal.TEN, true, LocalDateTime.now(), LocalDateTime.now());

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);

        OrderItem orderItem = new OrderItem(1L, 2, BigDecimal.TEN);
        orderItem.setId(1L);
        orderItem.setOrder(order);

        order.setOrderItems(Collections.singletonList(orderItem));

        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            if (savedOrder.getId() == null) {
                savedOrder.setId(1L);
            }
            return savedOrder;
        });
        when(productCatalogService.getProductById(1L)).thenReturn(CompletableFuture.completedFuture(productResponse));
        when(inventoryService.reserveInventory(1L, 2)).thenReturn(CompletableFuture.completedFuture(null));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
            OrderItem savedOrderItem = invocation.getArgument(0);
            if (savedOrderItem.getId() == null) {
                savedOrderItem.setId(1L);
            }
            savedOrderItem.calculateSubtotal();
            return savedOrderItem;
        });

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.createOrder(orderRequest);
        OrderResponse orderResponse = future.get();

        // Then
        assertNotNull(orderResponse);
        assertEquals(1L, orderResponse.id());
        assertEquals(1L, orderResponse.customerId());
        assertEquals(OrderStatus.PENDING.toString(), orderResponse.status());
        assertNotNull(orderResponse.orderItems());
        assertEquals(1, orderResponse.orderItems().size());
        assertEquals(1L, orderResponse.orderItems().get(0).productId());
        assertEquals(BigDecimal.valueOf(20), orderResponse.totalAmount());
    }

    @Test
    void getOrder_shouldReturnOrderSuccessfully() throws Exception {
        // Given
        Long orderId = 1L;
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.emptyList());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.getOrder(orderId);
        OrderResponse orderResponse = future.get();

        // Then
        assertNotNull(orderResponse);
        assertEquals(orderId, orderResponse.id());
        assertEquals(1L, orderResponse.customerId());
    }

    @Test
    void getOrder_whenOrderNotFound_shouldThrowException() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.getOrder(orderId);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof ResourceNotFoundException);
                throw e;
            }
        });
    }

    @Test
    void getCustomerOrders_shouldReturnPagedResponse() throws Exception {
        // Given
        Long customerId = 1L;
        Pageable pageable = Pageable.ofSize(10);
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(customerId);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.emptyList());

        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order), pageable, 1);

        when(orderRepository.findByCustomerId(customerId, pageable)).thenReturn(orderPage);
        when(userService.getUserProfile(customerId)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<PagedResponse<OrderResponse>> future = orderServiceImpl.getCustomerOrders(customerId, pageable);
        PagedResponse<OrderResponse> pagedResponse = future.get();

        // Then
        assertNotNull(pagedResponse);
        assertEquals(1, pagedResponse.content().size());
        assertEquals(1L, pagedResponse.totalElements());
    }

    @Test
    void getCustomerOrders_whenCustomerNotFound_shouldThrowException() {
        // Given
        Long customerId = 1L;
        Pageable pageable = Pageable.ofSize(10);
        when(userService.getUserProfile(customerId)).thenReturn(CompletableFuture.failedFuture(new ResourceNotFoundException("User not found")));

        // When
        CompletableFuture<PagedResponse<OrderResponse>> future = orderServiceImpl.getCustomerOrders(customerId, pageable);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof ResourceNotFoundException);
                throw e;
            }
        });
    }

    @Test
    void updateOrderStatus_shouldUpdateStatusSuccessfully() throws Exception {
        // Given
        Long orderId = 1L;
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAID.toString(), "Payment received");
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.emptyList());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.updateOrderStatus(orderId, request);
        OrderResponse orderResponse = future.get();

        // Then
        assertNotNull(orderResponse);
        assertEquals(OrderStatus.PAID.toString(), orderResponse.status());
        assertEquals("Payment received", orderResponse.notes());
    }

    @Test
    void updateOrderStatus_whenOrderNotFound_shouldThrowException() {
        // Given
        Long orderId = 1L;
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAID.toString(), null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.updateOrderStatus(orderId, request);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof ResourceNotFoundException);
                throw e;
            }
        });
    }

    @Test
    void updateOrderStatus_whenInvalidTransition_shouldThrowException() {
        // Given
        Long orderId = 1L;
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.SHIPPED.toString(), null);
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.updateOrderStatus(orderId, request);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void processOrderPayment_shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Payment successful");
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(paymentRequest)).thenReturn(CompletableFuture.completedFuture(paymentResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<PaymentResponse> future = orderServiceImpl.processOrderPayment(paymentRequest);
        PaymentResponse response = future.get();

        // Then
        assertNotNull(response);
        assertEquals("SUCCESS", response.status());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("payment-1", order.getPaymentId());
    }

    @Test
    void processOrderPayment_whenOrderNotFound_shouldThrowException() {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        CompletableFuture<PaymentResponse> future = orderServiceImpl.processOrderPayment(paymentRequest);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof ResourceNotFoundException);
                throw e;
            }
        });
    }

    @Test
    void processOrderPayment_whenInvalidOrderStatus_shouldThrowException() {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<PaymentResponse> future = orderServiceImpl.processOrderPayment(paymentRequest);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void processOrderPayment_whenPaymentFails_shouldThrowException() {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "FAILED", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Payment failed");
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(paymentRequest)).thenReturn(CompletableFuture.completedFuture(paymentResponse));
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<PaymentResponse> future = orderServiceImpl.processOrderPayment(paymentRequest);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(PaymentProcessingException.class);
    }

    @Test
    void cancelOrder_shouldCancelOrderSuccessfully() throws Exception {
        // Given
        Long orderId = 1L;
        String reason = "No longer needed";
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());
        ProductResponse productResponse = new ProductResponse(1L, "Test Product", "Description", BigDecimal.TEN, true, LocalDateTime.now(), LocalDateTime.now());

        OrderItem orderItem = new OrderItem(1L, 2, BigDecimal.TEN);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.singletonList(orderItem));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(inventoryService.restoreInventory(1L, 2)).thenReturn(CompletableFuture.completedFuture(null));
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));
        when(productCatalogService.getProductById(1L)).thenReturn(CompletableFuture.completedFuture(productResponse));

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.cancelOrder(orderId, reason);
        OrderResponse orderResponse = future.get();

        // Then
        assertNotNull(orderResponse);
        assertEquals(OrderStatus.CANCELLED.toString(), orderResponse.status());
        assertEquals(reason, orderResponse.notes());
    }

    @Test
    void cancelOrder_whenOrderNotFound_shouldThrowException() {
        // Given
        Long orderId = 1L;
        String reason = "No longer needed";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.cancelOrder(orderId, reason);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof ResourceNotFoundException);
                throw e;
            }
        });
    }

    @Test
    void cancelOrder_whenInvalidTransition_shouldThrowException() {
        // Given
        Long orderId = 1L;
        String reason = "No longer needed";
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.cancelOrder(orderId, reason);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void getAllOrders_shouldReturnPagedResponse() throws Exception {
        // Given
        Pageable pageable = Pageable.ofSize(10);
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", null, "ROLE_CUSTOMER", true, LocalDateTime.now());

        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.emptyList());

        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order), pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(userService.getUserProfile(1L)).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When
        CompletableFuture<PagedResponse<OrderResponse>> future = orderServiceImpl.getAllOrders(pageable);
        PagedResponse<OrderResponse> pagedResponse = future.get();

        // Then
        assertNotNull(pagedResponse);
        assertEquals(1, pagedResponse.content().size());
        assertEquals(1L, pagedResponse.totalElements());
    }
}