package com.ecommerce.orderprocessing.order.controller;

import com.ecommerce.orderprocessing.order.service.OrderService;
import com.ecommerce.orderprocessing.order.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.common.dto.PagedResponse;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT') or @orderRepository.findById(#orderId).orElse(null)?.customer.id == authentication.principal.id")
    public CompletableFuture<OrderResponse> getOrder(@PathVariable Long orderId, Authentication authentication) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT') or #customerId == authentication.principal.id")
    public CompletableFuture<PagedResponse<OrderResponse>> getCustomerOrders(@PathVariable Long customerId, Pageable pageable, Authentication authentication) {
        return orderService.getCustomerOrders(customerId, pageable);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER')")
    public CompletableFuture<OrderResponse> updateOrderStatus(@PathVariable Long orderId, @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(orderId, request);
    }

    @PostMapping("/payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<PaymentResponse> processOrderPayment(@RequestBody PaymentRequest paymentRequest) {
        return orderService.processOrderPayment(paymentRequest);
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER') or @orderRepository.findById(#orderId).orElse(null)?.customer.id == authentication.principal.id")
    public CompletableFuture<OrderResponse> cancelOrder(@PathVariable Long orderId, @RequestParam String reason, Authentication authentication) {
        return orderService.cancelOrder(orderId, reason);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT')")
    public CompletableFuture<PagedResponse<OrderResponse>> getAllOrders(Pageable pageable) {
        return orderService.getAllOrders(pageable);
    }
}
