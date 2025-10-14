package com.ecommerce.orderprocessing.shoppingcart.repository;

import com.ecommerce.orderprocessing.shoppingcart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}