package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Order;
import com.ecommerce.orderprocessing.domain.entity.OrderItem;
import com.ecommerce.orderprocessing.domain.enumeration.OrderStatus;
import com.ecommerce.orderprocessing.dto.request.CreateOrderItemRequest;
import com.ecommerce.orderprocessing.dto.request.CreateOrderRequest;
import com.ecommerce.orderprocessing.dto.request.PaymentRequest;
import com.ecommerce.orderprocessing.dto.request.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.dto.response.OrderItemResponse;
import com.ecommerce.orderprocessing.dto.response.OrderResponse;
import com.ecommerce.orderprocessing.dto.response.PagedResponse;
import com.ecommerce.orderprocessing.dto.response.PaymentResponse;
import com.ecommerce.orderprocessing.exception.InvalidOrderStateException;
import com.ecommerce.orderprocessing.exception.PaymentProcessingException;
import com.ecommerce.orderprocessing.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.repository.CustomerRepository;
import com.ecommerce.orderprocessing.repository.OrderItemRepository;
import com.ecommerce.orderprocessing.repository.OrderRepository;
import com.ecommerce.orderprocessing.repository.ProductRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final ExecutorService virtualThreadExecutor;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                            CustomerRepository customerRepository, ProductRepository productRepository,
                            InventoryService inventoryService, PaymentService paymentService,                            
                            ExecutorService virtualThreadExecutor) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    public CompletableFuture<OrderResponse> createOrder(CreateOrderRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            var customer = customerRepository.findById(request.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            var order = new Order();
            order.setCustomer(customer);
            order.setStatus(OrderStatus.PENDING);

            var savedOrder = orderRepository.save(order);

            List<CompletableFuture<OrderItem>> futures = request.orderItems().stream()
                    .map(itemRequest -> createOrderItem(itemRequest, savedOrder))
                    .collect(Collectors.toList());

            List<OrderItem> orderItems = futures.stream()
                    .map(CompletableFuture::join)
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
            var customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

            var orderPage = orderRepository.findByCustomer(customer, pageable);

            List<OrderResponse> orderResponses = orderPage.getContent().stream()
                    .map(order -> toOrderResponse(order, order.getOrderItems()))
                    .collect(Collectors.toList());

            return new PagedResponse<>(
                    orderResponses,
                    orderPage.getNumber(),
                    orderPage.getSize(),
                    orderPage.getTotalElements(),
                    orderPage.getTotalPages()
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

            if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PAID) {
                order.getOrderItems().forEach(orderItem -> {
                    inventoryService.restoreInventory(orderItem.getProduct().getId(), orderItem.getQuantity());
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
                    orderPage.getTotalPages()
            );
        }, virtualThreadExecutor);
    }

    private CompletableFuture<OrderItem> createOrderItem(CreateOrderItemRequest itemRequest, Order order) {
        return CompletableFuture.supplyAsync(() -> {
            var product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            inventoryService.reserveInventory(product.getId(), itemRequest.quantity()).join();

            var orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(product.getPrice());

            return orderItemRepository.save(orderItem);
        }, virtualThreadExecutor);
    }

    private OrderResponse toOrderResponse(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail(),
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
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getSubtotal()
        );
    }
}
