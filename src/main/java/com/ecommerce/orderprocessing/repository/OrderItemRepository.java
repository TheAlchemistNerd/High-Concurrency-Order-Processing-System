package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Order;
import com.ecommerce.orderprocessing.domain.entity.OrderItem;
import com.ecommerce.orderprocessing.domain.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for OrderItem entity.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    List<OrderItem> findByProduct(Product product);

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.product.id = :productId")
    Integer getTotalQuantitySoldForProduct(@Param("productId") Long productId);

    /**
     * Get best-selling products using a type-safe record projection.
     */
    @Query("""
        SELECT new com.ecommerce.orderprocessing.repository.BestSellingProduct(oi.product, SUM(oi.quantity))
        FROM OrderItem oi
        GROUP BY oi.product
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<BestSellingProduct> getBestSellingProducts(Pageable pageable);

    @Query("""
        SELECT oi FROM OrderItem oi 
        WHERE oi.order.createdAt BETWEEN :startDate AND :endDate
    """)
    List<OrderItem> findOrderItemsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(oi.subtotal), java.math.BigDecimal.ZERO) FROM OrderItem oi WHERE oi.product.id = :productId")
    BigDecimal getTotalRevenueForProduct(@Param("productId") Long productId);

    @Query("""
        SELECT COALESCE(SUM(oi.subtotal), java.math.BigDecimal.ZERO) 
        FROM OrderItem oi 
        WHERE oi.order.createdAt BETWEEN :startDate AND :endDate
    """)
    BigDecimal getTotalRevenueForPeriod(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
