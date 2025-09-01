package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.enumeration.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Customer entity.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByIsActiveTrue();

    List<Customer> findByRole(UserRole role);

    @Query("""
        SELECT c FROM Customer c
        WHERE c.createdAt BETWEEN :startDate AND :endDate
    """)
    List<Customer> findCustomersCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    long countByIsActiveTrue();

    @Query("SELECT DISTINCT c FROM Customer c JOIN c.orders")
    List<Customer> findCustomersWithOrders();
}