package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.entity.Order;
import com.ecommerce.orderprocessing.domain.entity.OrderItem;
import com.ecommerce.orderprocessing.domain.entity.Product;
import com.ecommerce.orderprocessing.domain.enumeration.OrderStatus;
import com.ecommerce.orderprocessing.dto.request.CreateOrderItemRequest;
import com.ecommerce.orderprocessing.dto.request.CreateOrderRequest;
import com.ecommerce.orderprocessing.dto.request.PaymentRequest;
import com.ecommerce.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.dto.response.PaymentResponse;
import com.ecommerce.orderprocessing.exception.PaymentProcessingException;
import com.ecommerce.orderprocessing.dto.response.OrderResponse;
import com.ecommerce.orderprocessing.dto.response.PagedResponse;
import com.ecommerce.orderprocessing.exception.InvalidOrderStateException;
import com.ecommerce.orderprocessing.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.ecommerce.orderprocessing.repository.CustomerRepository;
import com.ecommerce.orderprocessing.repository.OrderItemRepository;
import com.ecommerce.orderprocessing.repository.OrderRepository;
import com.ecommerce.orderprocessing.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentService paymentService;

    private ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();


    private OrderServiceImpl orderServiceImpl;

    @BeforeEach
    void setUp() {
        orderServiceImpl = new OrderServiceImpl(orderRepository, orderItemRepository, customerRepository, productRepository, inventoryService, paymentService, virtualThreadExecutor);
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() throws Exception {
        // Given
        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest(1L, 2);
        CreateOrderRequest orderRequest = new CreateOrderRequest(1L, "123 Main St", Collections.singletonList(itemRequest), "notes");

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setEmail("test@test.com");

        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setPrice(BigDecimal.TEN);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(BigDecimal.TEN);
        orderItem.calculateSubtotal();

        order.setOrderItems(Collections.singletonList(orderItem));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            if (savedOrder.getId() == null) {
                savedOrder.setId(1L);
            }
            return savedOrder;
        });
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
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
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Customer");
        customer.setEmail("test@test.com");

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.emptyList());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

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
        Customer customer = new Customer();
        customer.setId(customerId);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.emptyList());

        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order), pageable, 1);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomer(customer, pageable)).thenReturn(orderPage);

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
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

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

        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.emptyList());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

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

        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.updateOrderStatus(orderId, request);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof InvalidOrderStateException);
                throw e;
            }
        });
    }

    @Test
    void processOrderPayment_shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Payment successful");

        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(paymentRequest)).thenReturn(CompletableFuture.completedFuture(paymentResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

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

        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // When
        CompletableFuture<PaymentResponse> future = orderServiceImpl.processOrderPayment(paymentRequest);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof InvalidOrderStateException);
                throw e;
            }
        });
    }

    @Test
    void processOrderPayment_whenPaymentFails_shouldThrowException() {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "FAILED", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Payment failed");

        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentService.processPayment(paymentRequest)).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        // When
        CompletableFuture<PaymentResponse> future = orderServiceImpl.processOrderPayment(paymentRequest);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof PaymentProcessingException);
                throw e;
            }
        });
    }

    @Test
    void cancelOrder_shouldCancelOrderSuccessfully() throws Exception {
        // Given
        Long orderId = 1L;
        String reason = "No longer needed";

        Customer customer = new Customer();
        customer.setId(1L);

        Product product = new Product();
        product.setId(1L);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(2);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.singletonList(orderItem));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

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

        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.SHIPPED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        CompletableFuture<OrderResponse> future = orderServiceImpl.cancelOrder(orderId, reason);

        // Then
        assertThrows(Exception.class, () -> {
            try {
                future.get();
            } catch (Exception e) {
                assertTrue(e.getCause() instanceof InvalidOrderStateException);
                throw e;
            }
        });
    }

    @Test
    void getAllOrders_shouldReturnPagedResponse() throws Exception {
        // Given
        Pageable pageable = Pageable.ofSize(10);
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order();
        order.setId(1L);
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderItems(Collections.emptyList());

        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order), pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // When
        CompletableFuture<PagedResponse<OrderResponse>> future = orderServiceImpl.getAllOrders(pageable);
        PagedResponse<OrderResponse> pagedResponse = future.get();

        // Then
        assertNotNull(pagedResponse);
        assertEquals(1, pagedResponse.content().size());
        assertEquals(1L, pagedResponse.totalElements());
    }
}