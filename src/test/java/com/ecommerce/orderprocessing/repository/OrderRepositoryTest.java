package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.entity.Order;
import com.ecommerce.orderprocessing.domain.enumeration.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
    void findByCustomer_shouldReturnOrders() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order = new Order(customer, "Address 1");
        entityManager.persistAndFlush(order);

        // When
        Page<Order> orders = orderRepository.findByCustomer(customer, PageRequest.of(0, 10));

        // Then
        assertThat(orders).hasSize(1);
    }

    @Test
    void findByCustomerId_shouldReturnOrders() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order = new Order(customer, "Address 1");
        entityManager.persistAndFlush(order);

        // When
        Page<Order> orders = orderRepository.findByCustomerId(customer.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(orders).hasSize(1);
    }

    @Test
    void findByStatus_shouldReturnOrders() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order = new Order(customer, "Address 1");
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
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order = new Order(customer, "Address 1");
        order.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(order);

        // When
        List<Order> orders = orderRepository.findOrdersCreatedBetween(LocalDateTime.now().minusDays(2), LocalDateTime.now());

        // Then
        assertThat(orders).hasSize(1);
    }

    @Test
    void findByCustomerAndStatus_shouldReturnOrders() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order = new Order(customer, "Address 1");
        order.setStatus(OrderStatus.PENDING);
        entityManager.persistAndFlush(order);

        // When
        List<Order> orders = orderRepository.findByCustomerAndStatus(customer, OrderStatus.PENDING);

        // Then
        assertThat(orders).hasSize(1);
    }

    @Test
    void countByStatus_shouldReturnCount() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order = new Order(customer, "Address 1");
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
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order1 = new Order(customer, "Address 1");
        order1.setCreatedAt(LocalDateTime.now().minusDays(1));
        entityManager.persistAndFlush(order1);

        Order order2 = new Order(customer, "Address 2");
        order2.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(order2);

        // When
        List<Order> orders = orderRepository.findRecentOrdersByCustomerId(customer.getId(), PageRequest.of(0, 1));

        // Then
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0)).isEqualTo(order2);
    }

    @Test
    void findByPaymentId_shouldReturnOrder() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order = new Order(customer, "Address 1");
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
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        entityManager.persistAndFlush(customer);

        Order order1 = new Order(customer, "Address 1");
        order1.setStatus(OrderStatus.PENDING);
        entityManager.persistAndFlush(order1);

        Order order2 = new Order(customer, "Address 2");
        order2.setStatus(OrderStatus.PENDING);
        entityManager.persistAndFlush(order2);

        Order order3 = new Order(customer, "Address 3");
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