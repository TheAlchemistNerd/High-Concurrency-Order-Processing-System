package com.ecommerce.orderprocessing.order.repository;


import com.ecommerce.orderprocessing.order.domain.entity.Order;
import com.ecommerce.orderprocessing.order.domain.entity.OrderItem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import com.ecommerce.orderprocessing.common.AbstractContainerBaseTest;

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
        Long customerId = 1L; // Dummy customerId
        Long productId = 1L; // Dummy productId

        Order order = new Order(customerId, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem = new OrderItem(productId, 1, BigDecimal.TEN);
        orderItem.setOrder(order);
        entityManager.persistAndFlush(orderItem);

        // When
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        // Then
        assertThat(orderItems).hasSize(1);
    }



    @Test
    void getTotalQuantitySoldForProduct_shouldReturnTotalQuantity() {
        // Given
        Long customerId = 1L; // Dummy customerId
        Long productId = 1L; // Dummy productId

        Order order = new Order(customerId, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem1 = new OrderItem(productId, 2, BigDecimal.TEN);
        orderItem1.setOrder(order);
        entityManager.persistAndFlush(orderItem1);

        OrderItem orderItem2 = new OrderItem(productId, 3, BigDecimal.TEN);
        orderItem2.setOrder(order);
        entityManager.persistAndFlush(orderItem2);

        // When
        Integer totalQuantity = orderItemRepository.getTotalQuantitySoldForProduct(productId);

        // Then
        assertThat(totalQuantity).isEqualTo(5);
    }

    @Test
    void getBestSellingProducts_shouldReturnBestSelling() {
        // Given
        Long customerId = 1L; // Dummy customerId
        Long product1Id = 1L; // Dummy productId
        Long product2Id = 2L; // Dummy productId

        Order order = new Order(customerId, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem1 = new OrderItem(product1Id, 5, BigDecimal.TEN);
        orderItem1.setOrder(order);
        entityManager.persistAndFlush(orderItem1);

        OrderItem orderItem2 = new OrderItem(product2Id, 10, BigDecimal.TEN);
        orderItem2.setOrder(order);
        entityManager.persistAndFlush(orderItem2);

        // When
        List<BestSellingProduct> bestSellingProducts = orderItemRepository.getBestSellingProducts(PageRequest.of(0, 1));

        // Then
        assertThat(bestSellingProducts).hasSize(1);
        assertThat(bestSellingProducts.get(0).productId()).isEqualTo(product2Id);
    }

    @Test
    void findOrderItemsByDateRange_shouldReturnOrderItems() {
        // Given
        Long customerId = 1L; // Dummy customerId
        Long productId = 1L; // Dummy productId

        Order order = new Order(customerId, "Address 1");
        order.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(order);

        OrderItem orderItem = new OrderItem(productId, 1, BigDecimal.TEN);
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
        Long customerId = 1L; // Dummy customerId
        Long productId = 1L; // Dummy productId

        Order order = new Order(customerId, "Address 1");
        entityManager.persistAndFlush(order);

        OrderItem orderItem1 = new OrderItem(productId, 2, BigDecimal.TEN);
        orderItem1.setOrder(order);
        entityManager.persistAndFlush(orderItem1);

        OrderItem orderItem2 = new OrderItem(productId, 3, BigDecimal.TEN);
        orderItem2.setOrder(order);
        entityManager.persistAndFlush(orderItem2);

        // When
        BigDecimal totalRevenue = orderItemRepository.getTotalRevenueForProduct(productId);

        // Then
        assertThat(totalRevenue).isEqualByComparingTo(BigDecimal.valueOf(50));
    }

    @Test
    void getTotalRevenueForPeriod_shouldReturnTotalRevenue() {
        // Given
        Long customerId = 1L; // Dummy customerId
        Long productId = 1L; // Dummy productId

        Order order = new Order(customerId, "Address 1");
        order.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(order);

        OrderItem orderItem = new OrderItem(productId, 2, BigDecimal.TEN);
        orderItem.setOrder(order);
        entityManager.persistAndFlush(orderItem);

        // When
        BigDecimal totalRevenue = orderItemRepository.getTotalRevenueForPeriod(LocalDateTime.now().minusDays(2), LocalDateTime.now());

        // Then
        assertThat(totalRevenue).isEqualByComparingTo(BigDecimal.valueOf(20));
    }
}
