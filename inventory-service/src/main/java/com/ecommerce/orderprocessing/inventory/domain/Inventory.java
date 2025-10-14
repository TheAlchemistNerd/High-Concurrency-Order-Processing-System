package com.ecommerce.orderprocessing.inventory.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId; // Link to Product Catalog Service

    @Column(nullable = false)
    private Integer stockQuantity;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Inventory() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Inventory(Long productId, Integer stockQuantity) {
        this();
        this.productId = productId;
        this.stockQuantity = stockQuantity;
    }

    public boolean hasStock(int requestedQuantity) {
        return this.stockQuantity >= requestedQuantity;
    }

    public void reduceStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new com.ecommerce.orderprocessing.inventory.exception.InsufficientStockException("Product", quantity, this.stockQuantity);
        }
        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void restoreStock(int quantity) {
        this.stockQuantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void commitStock(int quantity) {
        if (this.stockQuantity < quantity) {
            throw new com.ecommerce.orderprocessing.inventory.exception.InsufficientStockException("Product", quantity, this.stockQuantity);
        }
        this.stockQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(id, inventory.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}