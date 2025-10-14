package com.ecommerce.orderprocessing.order.controller;

import com.ecommerce.orderprocessing.order.service.OrderService;
import com.ecommerce.orderprocessing.order.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.payment.PaymentRequest;
import com.ecommerce.orderprocessing.common.dto.PagedResponse;
import com.ecommerce.orderprocessing.payment.PaymentResponse;
import org.springframework.data.domain.Pageable;
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
    public CompletableFuture<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public CompletableFuture<OrderResponse> getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/customer/{customerId}")
    public CompletableFuture<PagedResponse<OrderResponse>> getCustomerOrders(@PathVariable Long customerId, Pageable pageable) {
        return orderService.getCustomerOrders(customerId, pageable);
    }

    @PutMapping("/{orderId}/status")
    public CompletableFuture<OrderResponse> updateOrderStatus(@PathVariable Long orderId, @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(orderId, request);
    }

    @PostMapping("/payment")
    public CompletableFuture<PaymentResponse> processOrderPayment(@RequestBody PaymentRequest paymentRequest) {
        return orderService.processOrderPayment(paymentRequest);
    }

    @PutMapping("/{orderId}/cancel")
    public CompletableFuture<OrderResponse> cancelOrder(@PathVariable Long orderId, @RequestParam String reason) {
        return orderService.cancelOrder(orderId, reason);
    }

    @GetMapping
    public CompletableFuture<PagedResponse<OrderResponse>> getAllOrders(Pageable pageable) {
        return orderService.getAllOrders(pageable);
    }
}
