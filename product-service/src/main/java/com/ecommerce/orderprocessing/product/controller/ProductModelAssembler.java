package com.ecommerce.orderprocessing.product.controller;

import com.ecommerce.orderprocessing.inventory.controller.InventoryController;
import com.ecommerce.orderprocessing.product.ProductResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductModelAssembler implements RepresentationModelAssembler<ProductResponse, EntityModel<ProductResponse>> {

    @Override
    public EntityModel<ProductResponse> toModel(ProductResponse product) {
        return EntityModel.of(product,
                linkTo(methodOn(ProductController.class).getProductById(product.id())).withSelfRel(),
                linkTo(methodOn(ProductController.class).getAllProducts()).withRel("products"),
                linkTo(methodOn(InventoryController.class).getInventoryByProductId(product.id())).withRel("inventory")
        );
    }
}
