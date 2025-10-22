package com.ecommerce.orderprocessing.product.service;

import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.product.Product;
import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    // Use a real ExecutorService for CompletableFuture testing
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @InjectMocks
    private ProductCatalogServiceImpl productCatalogService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Manually inject the real ExecutorService into the service under test
        productCatalogService = new ProductCatalogServiceImpl(productRepository, virtualThreadExecutor);

        product1 = new Product();
        product1.setId(1L);
        product1.setName("Laptop");
        product1.setDescription("Powerful laptop");
        product1.setPrice(BigDecimal.valueOf(1200.00));
        product1.setIsActive(true);
        product1.setCreatedAt(LocalDateTime.now());
        product1.setUpdatedAt(LocalDateTime.now());

        product2 = new Product();
        product2.setId(2L);
        product2.setName("Mouse");
        product2.setDescription("Wireless mouse");
        product2.setPrice(BigDecimal.valueOf(25.00));
        product2.setIsActive(true);
        product2.setCreatedAt(LocalDateTime.now());
        product2.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getProductById_shouldReturnProductResponse() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        ProductResponse response = productCatalogService.getProductById(1L).get();

        assertNotNull(response);
        assertEquals(product1.getId(), response.id());
        assertEquals(product1.getName(), response.name());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_shouldThrowExceptionWhenNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productCatalogService.getProductById(99L).join());
        verify(productRepository, times(1)).findById(99L);
    }

    @Test
    void getAllProducts_shouldReturnListOfProductResponses() throws Exception {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        List<ProductResponse> responses = productCatalogService.getAllProducts().get();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(product1.getId(), responses.get(0).id());
        assertEquals(product2.getId(), responses.get(1).id());
        verify(productRepository, times(1)).findAll();
    }
}