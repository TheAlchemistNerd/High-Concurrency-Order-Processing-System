package com.ecommerce.orderprocessing.product.controller;

import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductCatalogService productCatalogService;

    private ProductResponse productResponse1;
    private ProductResponse productResponse2;

    @BeforeEach
    void setUp() {
        productResponse1 = new ProductResponse(1L, "Laptop", "Powerful laptop", BigDecimal.valueOf(1200.00), true, LocalDateTime.now(), LocalDateTime.now());
        productResponse2 = new ProductResponse(2L, "Mouse", "Wireless mouse", BigDecimal.valueOf(25.00), true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void getProductById_shouldReturnProductResponse() throws Exception {
        when(productCatalogService.getProductById(anyLong())).thenReturn(CompletableFuture.completedFuture(productResponse1));

        mockMvc.perform(get("/api/products/{productId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(productResponse1.id()))
                .andExpect(jsonPath(".name").value(productResponse1.name()));
    }

    @Test
    void getAllProducts_shouldReturnListOfProductResponses() throws Exception {
        List<ProductResponse> productResponses = Arrays.asList(productResponse1, productResponse2);
        when(productCatalogService.getAllProducts()).thenReturn(CompletableFuture.completedFuture(productResponses));

        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(productResponses.size()))
                .andExpect(jsonPath("$[0].id").value(productResponse1.id()))
                .andExpect(jsonPath("$[1].id").value(productResponse2.id()));
    }
}