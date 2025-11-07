package com.ecommerce.orderprocessing.order.controller;

import com.ecommerce.orderprocessing.order.service.OrderService;
import com.ecommerce.orderprocessing.order.dto.CreateOrderRequest;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.order.dto.UpdateOrderStatusRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.common.dto.PagedResponse;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderModelAssembler assembler;
    private final PaymentModelAssembler paymentAssembler;

    public OrderController(OrderService orderService, OrderModelAssembler assembler, PaymentModelAssembler paymentAssembler) {
        this.orderService = orderService;
        this.assembler = assembler;
        this.paymentAssembler = paymentAssembler;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<ResponseEntity<EntityModel<OrderResponse>>> createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request)
                .thenApply(assembler::toModel)
                .thenApply(orderModel -> ResponseEntity
                        .created(orderModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                        .body(orderModel));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT') or @orderRepository.findById(#orderId).orElse(null)?.customer.id == authentication.principal.id")
    public CompletableFuture<EntityModel<OrderResponse>> getOrder(@PathVariable Long orderId, Authentication authentication) {
        return orderService.getOrder(orderId)
                .thenApply(assembler::toModel);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT') or #customerId == authentication.principal.id")
    public CompletableFuture<PagedModel<EntityModel<OrderResponse>>> getCustomerOrders(@PathVariable Long customerId, Pageable pageable, Authentication authentication) {
        return orderService.getCustomerOrders(customerId, pageable)
                .thenApply(pagedResponse -> {
                    List<EntityModel<OrderResponse>> orderModels = pagedResponse.content().stream()
                            .map(assembler::toModel)
                            .collect(Collectors.toList());
                    return PagedModel.of(orderModels, new PagedModel.PageMetadata(pagedResponse.size(), pagedResponse.number(), pagedResponse.totalElements(), pagedResponse.totalPages()),
                            linkTo(methodOn(OrderController.class).getCustomerOrders(customerId, pageable, null)).withSelfRel());
                });
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER')")
    public CompletableFuture<EntityModel<OrderResponse>> updateOrderStatus(@PathVariable Long orderId, @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(orderId, request)
                .thenApply(assembler::toModel);
    }

    @PostMapping("/payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<EntityModel<PaymentResponse>> processOrderPayment(@RequestBody PaymentRequest paymentRequest) {
        return orderService.processOrderPayment(paymentRequest)
                .thenApply(paymentAssembler::toModel);
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER') or @orderRepository.findById(#orderId).orElse(null)?.customer.id == authentication.principal.id")
    public CompletableFuture<EntityModel<OrderResponse>> cancelOrder(@PathVariable Long orderId, @RequestParam String reason, Authentication authentication) {
        return orderService.cancelOrder(orderId, reason)
                .thenApply(assembler::toModel);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT')")
    public CompletableFuture<PagedModel<EntityModel<OrderResponse>>> getAllOrders(Pageable pageable) {
        return orderService.getAllOrders(pageable)
                .thenApply(pagedResponse -> {
                    List<EntityModel<OrderResponse>> orderModels = pagedResponse.content().stream()
                            .map(assembler::toModel)
                            .collect(Collectors.toList());
                    return PagedModel.of(orderModels, new PagedModel.PageMetadata(pagedResponse.size(), pagedResponse.number(), pagedResponse.totalElements(), pagedResponse.totalPages()),
                            linkTo(methodOn(OrderController.class).getAllOrders(pageable)).withSelfRel());
                });
    }
}
