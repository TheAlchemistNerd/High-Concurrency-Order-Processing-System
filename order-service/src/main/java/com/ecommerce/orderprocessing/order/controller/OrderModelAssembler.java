package com.ecommerce.orderprocessing.order.controller;

import com.ecommerce.orderprocessing.order.dto.OrderItemResponse;
import com.ecommerce.orderprocessing.order.dto.OrderResponse;
import com.ecommerce.orderprocessing.product.controller.ProductController;
import com.ecommerce.orderprocessing.user.controller.UserController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrderModelAssembler implements RepresentationModelAssembler<OrderResponse, EntityModel<OrderResponse>> {

    @Override
    public EntityModel<OrderResponse> toModel(OrderResponse order) {
        EntityModel<OrderResponse> orderModel = EntityModel.of(order,
                linkTo(methodOn(OrderController.class).getOrder(order.id(), null)).withSelfRel(),
                linkTo(methodOn(UserController.class).getUserProfileById(order.customerId())).withRel("customer")
        );

        // Conditional links based on order status
        if ("PENDING_PAYMENT".equals(order.status())) {
            orderModel.add(linkTo(methodOn(OrderController.class).processOrderPayment(null)).withRel("payment"));
        }
        if (!"CANCELLED".equals(order.status()) && !"DELIVERED".equals(order.status())) {
            orderModel.add(linkTo(methodOn(OrderController.class).cancelOrder(order.id(), null, null)).withRel("cancel"));
        }
        // Assuming update status is an admin/manager action, always available for relevant roles
        orderModel.add(linkTo(methodOn(OrderController.class).updateOrderStatus(order.id(), null)).withRel("updateStatus"));

        for (OrderItemResponse item : order.orderItems()) {
            orderModel.add(linkTo(methodOn(ProductController.class).getProductById(item.productId())).withRel("item-" + item.productId() + "-product"));
        }

        return orderModel;
    }
}
