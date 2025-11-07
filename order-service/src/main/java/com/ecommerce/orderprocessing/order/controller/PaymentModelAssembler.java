package com.ecommerce.orderprocessing.order.controller;

import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PaymentModelAssembler implements RepresentationModelAssembler<PaymentResponse, EntityModel<PaymentResponse>> {

    @Override
    public EntityModel<PaymentResponse> toModel(PaymentResponse payment) {
        return EntityModel.of(payment,
                linkTo(methodOn(OrderController.class).getOrder(payment.orderId(), null)).withRel("order")
        );
    }
}
