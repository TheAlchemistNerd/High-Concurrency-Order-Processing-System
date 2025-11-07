package com.ecommerce.orderprocessing.shoppingcart.controller;

import com.ecommerce.orderprocessing.order.controller.OrderController;
import com.ecommerce.orderprocessing.product.controller.ProductController;
import com.ecommerce.orderprocessing.shoppingcart.dto.CartItemResponse;
import com.ecommerce.orderprocessing.shoppingcart.dto.ShoppingCartResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ShoppingCartModelAssembler implements RepresentationModelAssembler<ShoppingCartResponse, EntityModel<ShoppingCartResponse>> {

    @Override
    public EntityModel<ShoppingCartResponse> toModel(ShoppingCartResponse cart) {
        EntityModel<ShoppingCartResponse> cartModel = EntityModel.of(cart,
                linkTo(methodOn(ShoppingCartController.class).getShoppingCart(cart.customerId(), null)).withSelfRel(),
                linkTo(methodOn(OrderController.class).createOrder(null)).withRel("checkout"),
                linkTo(methodOn(ShoppingCartController.class).clearShoppingCart(cart.customerId(), null)).withRel("clear")
        );

        for (CartItemResponse item : cart.items()) {
            Long customerId = cart.customerId();
            Long productId = item.productId();

            cartModel.add(linkTo(methodOn(ProductController.class).getProductById(productId)).withRel("item-" + productId + "-product"));
            cartModel.add(linkTo(methodOn(ShoppingCartController.class).updateItemQuantity(customerId, productId, null, null)).withRel("item-" + productId + "-update"));
            cartModel.add(linkTo(methodOn(ShoppingCartController.class).removeItemFromCart(customerId, productId, null)).withRel("item-" + productId + "-remove"));
        }

        return cartModel;
    }
}
