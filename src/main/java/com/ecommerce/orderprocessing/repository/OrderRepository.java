package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.entity.Order;
import com.ecommerce.orderprocessing.domain.enumeration.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId")
    Page<Order> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.createdAt BETWEEN :startDate AND :endDate
    """)
    List<Order> findOrdersCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);

    long countByStatus(OrderStatus status);

    @Query("""
        SELECT o FROM Order o
        WHERE o.customer.id = :customerId 
        ORDER BY o.createdAt DESC
    """)
    List<Order> findRecentOrdersByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    Optional<Order> findByPaymentId(String paymentId);

    /**
     * Get order statistics by status using a type-safe record projection.
     */
    @Query("""
        SELECT new com.ecommerce.orderprocessing.repository.OrderStatusStats(o.status, COUNT(o))
        FROM Order o
        GROUP BY o.status
    """)
    List<OrderStatusStats> getOrderStatsByStatus();
}

