package com.ecommerce.orderprocessing.inventory.controller;

import com.ecommerce.orderprocessing.inventory.dto.InventoryResponse;
import com.ecommerce.orderprocessing.product.controller.ProductController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventoryModelAssembler implements RepresentationModelAssembler<InventoryResponse, EntityModel<InventoryResponse>> {

    @Override
    public EntityModel<InventoryResponse> toModel(InventoryResponse inventory) {
        return EntityModel.of(inventory,
                linkTo(methodOn(InventoryController.class).getInventoryByProductId(inventory.productId())).withSelfRel(),
                linkTo(methodOn(ProductController.class).getProductById(inventory.productId())).withRel("product"),
                linkTo(methodOn(InventoryController.class).checkInventory(inventory.productId(), null)).withRel("check"),
                linkTo(methodOn(InventoryController.class).reserveInventory(inventory.productId(), null)).withRel("reserve"),
                linkTo(methodOn(InventoryController.class).releaseInventory(inventory.productId(), null)).withRel("release"),
                linkTo(methodOn(InventoryController.class).commitInventory(inventory.productId(), null)).withRel("commit"),
                linkTo(methodOn(InventoryController.class).restockInventory(inventory.productId(), null)).withRel("restock")
        );
    }
}
