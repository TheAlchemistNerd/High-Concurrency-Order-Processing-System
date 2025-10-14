package com.ecommerce.orderprocessing.order.service;

import com.ecommerce.orderprocessing.order.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.payment.PaymentRequest;
import com.ecommerce.orderprocessing.common.dto.PagedResponse;
import com.ecommerce.orderprocessing.payment.PaymentResponse;
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
