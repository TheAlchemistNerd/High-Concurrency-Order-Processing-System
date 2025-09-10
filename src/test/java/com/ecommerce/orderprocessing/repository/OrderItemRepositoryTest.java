package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.entity.Order;
import com.ecommerce.orderprocessing.domain.entity.OrderItem;
import com.ecommerce.orderprocessing.domain.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderItemRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void findByOrder_shouldReturnOrderItems() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product);

        Order order = new Order(customer, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem = new OrderItem(product, 1, BigDecimal.TEN);
        orderItem.setOrder(order);
        entityManager.persistAndFlush(orderItem);

        // When
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        // Then
        assertThat(orderItems).hasSize(1);
    }

    @Test
    void findByProduct_shouldReturnOrderItems() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product);

        Order order = new Order(customer, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem = new OrderItem(product, 1, BigDecimal.TEN);
        orderItem.setOrder(order);
        entityManager.persistAndFlush(orderItem);

        // When
        List<OrderItem> orderItems = orderItemRepository.findByProduct(product);

        // Then
        assertThat(orderItems).hasSize(1);
    }

    @Test
    void getTotalQuantitySoldForProduct_shouldReturnTotalQuantity() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product);

        Order order = new Order(customer, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem1 = new OrderItem(product, 2, BigDecimal.TEN);
        orderItem1.setOrder(order);
        entityManager.persistAndFlush(orderItem1);

        OrderItem orderItem2 = new OrderItem(product, 3, BigDecimal.TEN);
        orderItem2.setOrder(order);
        entityManager.persistAndFlush(orderItem2);

        // When
        Integer totalQuantity = orderItemRepository.getTotalQuantitySoldForProduct(product.getId());

        // Then
        assertThat(totalQuantity).isEqualTo(5);
    }

    @Test
    void getBestSellingProducts_shouldReturnBestSelling() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Product product1 = new Product("Product 1", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product1);

        Product product2 = new Product("Product 2", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product2);

        Order order = new Order(customer, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem1 = new OrderItem(product1, 5, BigDecimal.TEN);
        orderItem1.setOrder(order);
        entityManager.persistAndFlush(orderItem1);

        OrderItem orderItem2 = new OrderItem(product2, 10, BigDecimal.TEN);
        orderItem2.setOrder(order);
        entityManager.persistAndFlush(orderItem2);

        // When
        List<BestSellingProduct> bestSellingProducts = orderItemRepository.getBestSellingProducts(PageRequest.of(0, 1));

        // Then
        assertThat(bestSellingProducts).hasSize(1);
        assertThat(bestSellingProducts.get(0).product()).isEqualTo(product2);
    }

    @Test
    void findOrderItemsByDateRange_shouldReturnOrderItems() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product);

        Order order = new Order(customer, "Address 1");
        order.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(order);

        OrderItem orderItem = new OrderItem(product, 1, BigDecimal.TEN);
        orderItem.setOrder(order);
        entityManager.persistAndFlush(orderItem);

        // When
        List<OrderItem> orderItems = orderItemRepository.findOrderItemsByDateRange(LocalDateTime.now().minusDays(2), LocalDateTime.now());

        // Then
        assertThat(orderItems).hasSize(1);
    }

    @Test
    void getTotalRevenueForProduct_shouldReturnTotalRevenue() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product);

        Order order = new Order(customer, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem1 = new OrderItem(product, 2, BigDecimal.TEN);
        orderItem1.setOrder(order);
        entityManager.persistAndFlush(orderItem1);

        OrderItem orderItem2 = new OrderItem(product, 3, BigDecimal.TEN);
        orderItem2.setOrder(order);
        entityManager.persistAndFlush(orderItem2);

        // When
        BigDecimal totalRevenue = orderItemRepository.getTotalRevenueForProduct(product.getId());

        // Then
        assertThat(totalRevenue).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void getTotalRevenueForPeriod_shouldReturnTotalRevenue() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Product product = new Product("Test Product", "Description", BigDecimal.TEN, 10);
        entityManager.persistAndFlush(product);

        Order order = new Order(customer, "Address 1");
        order.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(order);

        OrderItem orderItem = new OrderItem(product, 2, BigDecimal.TEN);
        orderItem.setOrder(order);
        entityManager.persistAndFlush(orderItem);

        // When
        BigDecimal totalRevenue = orderItemRepository.getTotalRevenueForPeriod(LocalDateTime.now().minusDays(2), LocalDateTime.now());

        // Then
        assertThat(totalRevenue).isEqualByComparingTo(BigDecimal.valueOf(20));
    }
}
