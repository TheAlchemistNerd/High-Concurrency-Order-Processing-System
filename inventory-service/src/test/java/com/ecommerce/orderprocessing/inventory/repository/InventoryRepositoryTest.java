package com.ecommerce.orderprocessing.inventory.repository;

import com.ecommerce.orderprocessing.common.AbstractContainerBaseTest;
import com.ecommerce.orderprocessing.inventory.domain.Inventory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class InventoryRepositoryTest extends AbstractContainerBaseTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void findByProductId_shouldReturnInventory() {
        // Given
        Long productId = 1L;
        Inventory inventory = new Inventory(productId, 100);
        entityManager.persistAndFlush(inventory);

        // When
        Optional<Inventory> foundInventory = inventoryRepository.findByProductId(productId);

        // Then
        assertThat(foundInventory).isPresent();
        assertThat(foundInventory.get().getProductId()).isEqualTo(productId);
        assertThat(foundInventory.get().getStockQuantity()).isEqualTo(100);
    }

    @Test
    void findByProductId_shouldReturnEmptyWhenNotFound() {
        // Given
        Long nonExistentProductId = 999L;

        // When
        Optional<Inventory> foundInventory = inventoryRepository.findByProductId(nonExistentProductId);

        // Then
        assertThat(foundInventory).isEmpty();
    }
}