package com.ecommerce.orderprocessing.user.repository;

import com.ecommerce.orderprocessing.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    @Query("""
        SELECT u FROM User u
        WHERE c.createdAt BETWEEN :startDate AND :endDate
    """)
    List<User> findUsersCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    long countByIsActiveTrue();

    @Query("SELECT DISTINCT u FROM User u JOIN u.orders")
    List<User> findUsersWithOrders();
}