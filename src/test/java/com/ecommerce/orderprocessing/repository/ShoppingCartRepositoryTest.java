package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.entity.ShoppingCart;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShoppingCartRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    void findByCustomer_shouldReturnShoppingCart() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setCustomer(customer);
        entityManager.persistAndFlush(shoppingCart);

        // When
        ShoppingCart foundCart = shoppingCartRepository.findByCustomer(customer).orElse(null);

        // Then
        assertThat(foundCart).isNotNull();
        assertThat(foundCart.getCustomer()).isEqualTo(customer);
    }
}
