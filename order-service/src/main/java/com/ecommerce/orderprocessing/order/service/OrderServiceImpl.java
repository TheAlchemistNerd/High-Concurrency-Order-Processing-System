package com.ecommerce.orderprocessing.order.service;

import com.ecommerce.orderprocessing.order.domain.enumeration.OrderStatus;
import com.ecommerce.orderprocessing.order.domain.entity.Order;
import com.ecommerce.orderprocessing.order.domain.entity.OrderItem;
import com.ecommerce.orderprocessing.order.dto.*;
import com.ecommerce.orderprocessing.order.exception.InvalidOrderStateException;
import com.ecommerce.orderprocessing.order.repository.OrderItemRepository;
import com.ecommerce.orderprocessing.order.repository.OrderRepository;
import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.common.dto.PagedResponse;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import com.ecommerce.orderprocessing.payment.dto.RefundRequest;
import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.inventory.service.InventoryService;
import com.ecommerce.orderprocessing.payment.service.PaymentService;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import com.ecommerce.orderprocessing.payment.exception.PaymentProcessingException;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductCatalogService productCatalogService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final ExecutorService virtualThreadExecutor;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            ProductCatalogService productCatalogService,
                            InventoryService inventoryService, PaymentService paymentService,
                            UserService userService,
                            ExecutorService virtualThreadExecutor) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productCatalogService = productCatalogService;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    @Transactional
    public CompletableFuture<OrderResponse> createOrder(CreateOrderRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            var order = new Order();
            order.setCustomerId(request.customerId());
            order.setStatus(OrderStatus.PENDING);

            var savedOrder = orderRepository.save(order);

            List<OrderItem> orderItems = request.orderItems().stream()
                    .map(itemRequest -> createOrderItem(itemRequest, savedOrder).join())
                    .collect(Collectors.toList());

            BigDecimal totalAmount = orderItems.stream()
                    .map(OrderItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            savedOrder.setTotalAmount(totalAmount);
            orderRepository.save(savedOrder);

            return toOrderResponse(savedOrder, orderItems);
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<OrderResponse> getOrder(Long orderId) {
        return CompletableFuture.supplyAsync(() -> {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            return toOrderResponse(order, order.getOrderItems());
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<PagedResponse<OrderResponse>> getCustomerOrders(Long customerId, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            var orderPage = orderRepository.findByCustomerId(customerId, pageable);

            List<OrderResponse> orderResponses = orderPage.getContent().stream()
                    .map(order -> toOrderResponse(order, order.getOrderItems()))
                    .collect(Collectors.toList());

            return new PagedResponse<>(
                    orderResponses,
                    orderPage.getNumber(),
                    orderPage.getSize(),
                    orderPage.getTotalElements(),
                    orderPage.getTotalPages(),
                    orderPage.isFirst(),
                    orderPage.isLast(),
                    orderPage.hasNext(),
                    orderPage.hasPrevious()
            );
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<OrderResponse> updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            OrderStatus newStatus = OrderStatus.valueOf(request.status());

            if (!order.canTransitionTo(newStatus)) {
                throw new InvalidOrderStateException(order.getStatus().toString(), newStatus.toString());
            }

            order.updateStatus(newStatus);
            if (request.notes() != null) {
                order.setNotes(request.notes());
            }

            order = orderRepository.save(order);
            return toOrderResponse(order, order.getOrderItems());
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<PaymentResponse> processOrderPayment(PaymentRequest paymentRequest) {
        return CompletableFuture.supplyAsync(() -> {
            Order order = orderRepository.findById(paymentRequest.orderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            if (order.getStatus() != OrderStatus.PENDING) {
                throw new InvalidOrderStateException(order.getStatus().toString(), "PAID");
            }

            return paymentService.processPayment(paymentRequest).thenApply(paymentResponse -> {
                if ("SUCCESS".equals(paymentResponse.status())) {
                    order.setPaymentId(paymentResponse.paymentId());
                    order.updateStatus(OrderStatus.PAID);
                    orderRepository.save(order);
                } else {
                    throw new PaymentProcessingException(String.format("Payment failed for order %s: %s",
                            paymentResponse.paymentId(), paymentResponse.message()));
                }
                return paymentResponse;
            }).join();
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<OrderResponse> cancelOrder(Long orderId, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            if (!order.canTransitionTo(OrderStatus.CANCELLED)) {
                throw new InvalidOrderStateException(order.getStatus().toString(), "CANCELLED");
            }

            if (order.getStatus() == OrderStatus.PAID) {
                // Construct RefundRequest with necessary details
                RefundRequest refundRequest = new RefundRequest(
                        order.getPaymentId(),
                        order.getTotalAmount(),
                        reason, // Use the cancellation reason as refund reason
                        UUID.randomUUID().toString() // Generate a unique idempotency key
                );
                paymentService.refundPayment(refundRequest).join();
            }

            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PAID) {
                order.getOrderItems().forEach(orderItem -> {
                    inventoryService.releaseInventory(orderItem.getProductId(), orderItem.getQuantity());
                });
            }

            order.updateStatus(OrderStatus.CANCELLED);
            order.setNotes(reason);
            order = orderRepository.save(order);

            return toOrderResponse(order, order.getOrderItems());
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<PagedResponse<OrderResponse>> getAllOrders(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            var orderPage = orderRepository.findAll(pageable);

            List<OrderResponse> orderResponses = orderPage.getContent().stream()
                    .map(order -> toOrderResponse(order, order.getOrderItems()))
                    .collect(Collectors.toList());

            return new PagedResponse<>(
                    orderResponses,
                    orderPage.getNumber(),
                    orderPage.getSize(),
                    orderPage.getTotalElements(),
                    orderPage.getTotalPages(),
                    orderPage.isFirst(),
                    orderPage.isLast(),
                    orderPage.hasNext(),
                    orderPage.hasPrevious()
            );
        }, virtualThreadExecutor);
    }

    private CompletableFuture<OrderItem> createOrderItem(CreateOrderItemRequest itemRequest, Order order) {
        return CompletableFuture.supplyAsync(() -> {
            ProductResponse productResponse = productCatalogService.getProductById(itemRequest.productId()).join();

            inventoryService.reserveInventory(productResponse.id(), itemRequest.quantity()).join();

            var orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(productResponse.id());
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(productResponse.price());

            return orderItemRepository.save(orderItem);
        }, virtualThreadExecutor);
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());

        // Fetch customer details using UserService
        UserResponse userResponse = userService.getUserProfile(order.getCustomerId()).join();

        return new OrderResponse(
                order.getId(),
                userResponse.id(),
                userResponse.firstName() + " " + userResponse.lastName(),
                userResponse.email(),
                order.getStatus().toString(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getPaymentId(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                itemResponses
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        ProductResponse productResponse = productCatalogService.getProductById(orderItem.getProductId()).join();
        return new OrderItemResponse(
                orderItem.getId(),
                productResponse.id(),
                productResponse.name(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getSubtotal()
        );
    }
}
