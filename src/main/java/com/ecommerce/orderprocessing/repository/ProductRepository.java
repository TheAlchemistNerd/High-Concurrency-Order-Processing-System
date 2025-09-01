package com.ecommerce.orderprocessing.repository;


import com.ecommerce.orderprocessing.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.List;


/**
 * Repository for Product entity.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    @Query("""
        SELECT p FROM Product p 
        WHERE p.stockQuantity > :minStock AND p.isActive = true
    """)
    List<Product> findProductsWithStock(@Param("minStock") Integer minStock);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();

    @Query("""
        SELECT p FROM Product p 
        WHERE p.stockQuantity <= :threshold AND p.stockQuantity > 0 AND p.isActive = true
    """)
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    @Query("""
        SELECT p FROM Product p 
        WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true
    """)
    List<Product> findProductsByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    long countByIsActiveTrue();

    /**
     * Safely reduces the stock for a given product.
     * Returns the number of rows affected (1 for success, 0 for failure/insufficient stock).
     */
    @Modifying
    @Query("""
        UPDATE Product p 
        SET p.stockQuantity = p.stockQuantity - :quantity 
        WHERE p.id = :productId AND p.stockQuantity >= :quantity
    """)
    int reduceStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}

