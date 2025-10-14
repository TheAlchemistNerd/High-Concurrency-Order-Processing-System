package com.ecommerce.orderprocessing.shoppingcart.repository;

import com.ecommerce.orderprocessing.shoppingcart.domain.ShoppingCart;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import com.ecommerce.orderprocessing.common.AbstractContainerBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShoppingCartRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    void findByCustomerId_shouldReturnShoppingCart() {
        // Given
        Long customerId = 1L;

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCustomerId(customerId);
        entityManager.persistAndFlush(shoppingCart);

        // When
        ShoppingCart foundCart = shoppingCartRepository.findByCustomerId(customerId).orElse(null);

        // Then
        assertThat(foundCart).isNotNull();
        assertThat(foundCart.getCustomerId()).isEqualTo(customerId);
    }
}
