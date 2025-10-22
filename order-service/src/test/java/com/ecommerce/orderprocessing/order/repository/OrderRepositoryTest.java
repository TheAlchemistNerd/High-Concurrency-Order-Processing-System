package com.ecommerce.orderprocessing.order.repository;


import com.ecommerce.orderprocessing.order.domain.entity.Order;
import com.ecommerce.orderprocessing.order.domain.enumeration.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.ecommerce.orderprocessing.common.AbstractContainerBaseTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OrderRepository orderRepository;



    @Test
    void findByCustomerId_shouldReturnOrders() {
        // Given
        Long customerId = 1L; // Dummy customerId

        Order order = new Order(customerId, "Address 1");
        entityManager.persistAndFlush(order);

        // When
        Page<Order> orders = orderRepository.findByCustomerId(customerId, PageRequest.of(0, 10));

        // Then
        assertThat(orders).hasSize(1);
    }

    @Test
    void findByStatus_shouldReturnOrders() {
        // Given
        Long customerId = 1L; // Dummy customerId

        Order order = new Order(customerId, "Address 1");
        order.setStatus(OrderStatus.PENDING);
        entityManager.persistAndFlush(order);

        // When
        Page<Order> orders = orderRepository.findByStatus(OrderStatus.PENDING, PageRequest.of(0, 10));

        // Then
        assertThat(orders).hasSize(1);
    }

    @Test
    void findOrdersCreatedBetween_shouldReturnOrders() {
        // Given
        Long customerId = 1L; // Dummy customerId

        Order order = new Order(customerId, "Address 1");
        order.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(order);

        // When
        List<Order> orders = orderRepository.findOrdersCreatedBetween(LocalDateTime.now().minusDays(2), LocalDateTime.now());

        // Then
        assertThat(orders).hasSize(1);
    }



    @Test
    void countByStatus_shouldReturnCount() {
        // Given
        Long customerId = 1L; // Dummy customerId

        Order order = new Order(customerId, "Address 1");
        order.setStatus(OrderStatus.PENDING);
        entityManager.persistAndFlush(order);

        // When
        long count = orderRepository.countByStatus(OrderStatus.PENDING);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findRecentOrdersByCustomerId_shouldReturnOrders() {
        // Given
        Long customerId = 1L; // Dummy customerId

        Order order1 = new Order(customerId, "Address 1");
        order1.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(order1);

        Order order2 = new Order(customerId, "Address 2");
        order2.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(order2);

        // When
        List<Order> orders = orderRepository.findRecentOrdersByCustomerId(customerId, PageRequest.of(0, 1));

        // Then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0)).isEqualTo(order2);
    }

    @Test
    void findByPaymentId_shouldReturnOrder() {
        // Given
        Long customerId = 1L; // Dummy customerId

        Order order = new Order(customerId, "Address 1");
        order.setPaymentId("payment123");
        entityManager.persistAndFlush(order);

        // When
        Order foundOrder = orderRepository.findByPaymentId("payment123").orElse(null);

        // Then
        assertThat(foundOrder).isNotNull();
        assertThat(foundOrder.getPaymentId()).isEqualTo("payment123");
    }

    @Test
    void getOrderStatsByStatus_shouldReturnStats() {
        // Given
        Long customerId = 1L; // Dummy customerId

        Order order1 = new Order(customerId, "Address 1");
        order1.setStatus(OrderStatus.PENDING);
        entityManager.persistAndFlush(order1);

        Order order2 = new Order(customerId, "Address 2");
        order2.setStatus(OrderStatus.PENDING);
        entityManager.persistAndFlush(order2);

        Order order3 = new Order(customerId, "Address 3");
        order3.setStatus(OrderStatus.PAID);
        entityManager.persistAndFlush(order3);

        // When
        List<OrderStatusStats> stats = orderRepository.getOrderStatsByStatus();

        // Then
        assertThat(stats).hasSize(2);
        assertThat(stats).extracting("status").contains(OrderStatus.PENDING, OrderStatus.PAID);
        assertThat(stats).extracting("count").contains(2L, 1L);
    }
}
