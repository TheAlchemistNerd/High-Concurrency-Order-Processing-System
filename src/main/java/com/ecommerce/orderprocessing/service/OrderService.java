package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.dto.request.CreateOrderRequest;
import com.ecommerce.orderprocessing.dto.request.PaymentRequest;
import com.ecommerce.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.dto.response.OrderResponse;
import com.ecommerce.orderprocessing.dto.response.PagedResponse;
import com.ecommerce.orderprocessing.dto.response.PaymentResponse;
import org.springframework.data.domain.Pageable;

import java.util.concurrent.CompletableFuture;

public interface OrderService {
    CompletableFuture<OrderResponse> createOrder(CreateOrderRequest request);

    CompletableFuture<OrderResponse> getOrder(Long orderId);

    CompletableFuture<PagedResponse<OrderResponse>> getCustomerOrders(Long customerId, Pageable pageable);

    CompletableFuture<OrderResponse> updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    CompletableFuture<PaymentResponse> processOrderPayment(PaymentRequest paymentRequest);

    CompletableFuture<OrderResponse> cancelOrder(Long orderId, String reason);

    CompletableFuture<PagedResponse<OrderResponse>> getAllOrders(Pageable pageable);
}
