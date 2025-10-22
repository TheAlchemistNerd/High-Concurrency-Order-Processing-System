package com.ecommerce.orderprocessing.product.repository;

import com.ecommerce.orderprocessing.product.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.ecommerce.orderprocessing.common.AbstractContainerBaseTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findByNameContainingIgnoreCase_shouldReturnProducts() {
        // Given
        Product product = new Product("Test Product", "Description", BigDecimal.TEN);
        entityManager.persistAndFlush(product);

        // When
        List<Product> products = productRepository.findByNameContainingIgnoreCase("test");

        // Then
        assertThat(products).hasSize(1);
    }

    @Test
    void findByIsActiveTrue_shouldReturnActiveProducts() {
        // Given
        Product activeProduct = new Product("Active Product", "Description", BigDecimal.TEN);
        activeProduct.setIsActive(true);
        entityManager.persistAndFlush(activeProduct);

        Product inactiveProduct = new Product("Inactive Product", "Description", BigDecimal.TEN);
        inactiveProduct.setIsActive(false);
        entityManager.persistAndFlush(inactiveProduct);

        // When
        Page<Product> products = productRepository.findByIsActiveTrue(PageRequest.of(0, 10));

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.getContent().get(0).getIsActive()).isTrue();
    }

    @Test
    void findProductsByPriceRange_shouldReturnProducts() {
        // Given
        Product productInPriceRange = new Product("In Range", "Description", BigDecimal.valueOf(15));
        entityManager.persistAndFlush(productInPriceRange);

        Product productOutOfRange = new Product("Out of Range", "Description", BigDecimal.valueOf(25));
        entityManager.persistAndFlush(productOutOfRange);

        // When
        List<Product> products = productRepository.findProductsByPriceRange(BigDecimal.TEN, BigDecimal.valueOf(20));

        // Then
        assertThat(products).hasSize(1);
    }

    @Test
    void countByIsActiveTrue_shouldReturnCount() {
        // Given
        Product activeProduct = new Product("Active Product", "Description", BigDecimal.TEN);
        activeProduct.setIsActive(true);
        entityManager.persistAndFlush(activeProduct);

        Product inactiveProduct = new Product("Inactive Product", "Description", BigDecimal.TEN);
        inactiveProduct.setIsActive(false);
        entityManager.persistAndFlush(inactiveProduct);

        // When
        long count = productRepository.countByIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(1);
    }
}
