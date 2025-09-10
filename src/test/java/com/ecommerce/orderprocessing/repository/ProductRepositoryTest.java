package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product);

        // When
        List<Product> products = productRepository.findByNameContainingIgnoreCase("test");

        // Then
        assertThat(products).hasSize(1);
    }

    @Test
    void findByIsActiveTrue_shouldReturnActiveProducts() {
        // Given
        Product activeProduct = new Product("Active Product", "Description", BigDecimal.TEN, 10);
        activeProduct.setIsActive(true);
        entityManager.persistAndFlush(activeProduct);

        Product inactiveProduct = new Product("Inactive Product", "Description", BigDecimal.TEN, 10);
        inactiveProduct.setIsActive(false);
        entityManager.persistAndFlush(inactiveProduct);

        // When
        Page<Product> products = productRepository.findByIsActiveTrue(PageRequest.of(0, 10));

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.getContent().get(0).getIsActive()).isTrue();
    }

    @Test
    void findProductsWithStock_shouldReturnProducts() {
        // Given
        Product productWithStock = new Product("With Stock", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(productWithStock);

        Product productWithoutStock = new Product("Without Stock", "Description", BigDecimal.TEN, 0);
        entityManager.persistAndFlush(productWithoutStock);

        // When
        List<Product> products = productRepository.findProductsWithStock(5);

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getStockQuantity()).isGreaterThan(5);
    }

    @Test
    void findOutOfStockProducts_shouldReturnProducts() {
        // Given
        Product productWithStock = new Product("With Stock", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(productWithStock);

        Product productWithoutStock = new Product("Without Stock", "Description", BigDecimal.TEN, 0);
        entityManager.persistAndFlush(productWithoutStock);

        // When
        List<Product> products = productRepository.findOutOfStockProducts();

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getStockQuantity()).isEqualTo(0);
    }

    @Test
    void findLowStockProducts_shouldReturnProducts() {
        // Given
        Product productWithLowStock = new Product("Low Stock", "Description", BigDecimal.TEN, 5);
        entityManager.persistAndFlush(productWithLowStock);

        Product productWithHighStock = new Product("High Stock", "Description", BigDecimal.TEN, 20);
        entityManager.persistAndFlush(productWithHighStock);

        // When
        List<Product> products = productRepository.findLowStockProducts(10);

        // Then
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getStockQuantity()).isLessThanOrEqualTo(10);
    }

    @Test
    void findProductsByPriceRange_shouldReturnProducts() {
        // Given
        Product productInPriceRange = new Product("In Range", "Description", BigDecimal.valueOf(15), 10);
        entityManager.persistAndFlush(productInPriceRange);

        Product productOutOfRange = new Product("Out of Range", "Description", BigDecimal.valueOf(25), 10);
        entityManager.persistAndFlush(productOutOfRange);

        // When
        List<Product> products = productRepository.findProductsByPriceRange(BigDecimal.TEN, BigDecimal.valueOf(20));

        // Then
        assertThat(products).hasSize(1);
    }

    @Test
    void countByIsActiveTrue_shouldReturnCount() {
        // Given
        Product activeProduct = new Product("Active Product", "Description", BigDecimal.TEN, 10);
        activeProduct.setIsActive(true);
        entityManager.persistAndFlush(activeProduct);

        Product inactiveProduct = new Product("Inactive Product", "Description", BigDecimal.TEN, 10);
        inactiveProduct.setIsActive(false);
        entityManager.persistAndFlush(inactiveProduct);

        // When
        long count = productRepository.countByIsActiveTrue();

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void reduceStock_shouldReduceStock() {
        // Given
        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product);

        // When
        int updatedRows = productRepository.reduceStock(product.getId(), 5);

        // Then
        assertThat(updatedRows).isEqualTo(1);
        Product updatedProduct = productRepository.findById(product.getId()).orElse(null);
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getStockQuantity()).isEqualTo(5);
    }
}
