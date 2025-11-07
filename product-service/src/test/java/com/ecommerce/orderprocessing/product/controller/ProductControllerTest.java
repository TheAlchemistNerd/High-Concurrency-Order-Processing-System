package com.ecommerce.orderprocessing.product.controller;

import com.ecommerce.orderprocessing.inventory.controller.InventoryController;
import com.ecommerce.orderprocessing.inventory.service.InventoryService;
import com.ecommerce.orderprocessing.product.ProductResponse;
import com.ecommerce.orderprocessing.product.dto.ProductRequest;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({ProductModelAssembler.class, InventoryController.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductCatalogService productCatalogService;

    @MockBean
    private InventoryService inventoryService; // Required for InventoryController link building

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse productResponse1;
    private ProductResponse productResponse2;

    @BeforeEach
    void setUp() {
        productResponse1 = new ProductResponse(1L, "Laptop", "Powerful laptop", BigDecimal.valueOf(1200.00), true, LocalDateTime.now(), LocalDateTime.now());
        productResponse2 = new ProductResponse(2L, "Mouse", "Wireless mouse", BigDecimal.valueOf(25.00), true, LocalDateTime.now(), LocalDateTime.now());
        // Default to no authentication
        SecurityContextHolder.clearContext();
    }

    private void setupAsAdmin() {
        AppUserDetails adminUserDetails = new AppUserDetails(1L, "admin@example.com", "password", "ADMIN", true, Collections.emptyMap());
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
    }

    private void setupAsCustomer() {
        AppUserDetails customerUserDetails = new AppUserDetails(2L, "customer@example.com", "password", "CUSTOMER", true, Collections.emptyMap());
        Authentication customerAuthentication = new UsernamePasswordAuthenticationToken(
                customerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(customerAuthentication);
    }

    @Test
    void getProductById_shouldReturnProductWithLinks() throws Exception {
        when(productCatalogService.getProductById(1L)).thenReturn(CompletableFuture.completedFuture(productResponse1));

        mockMvc.perform(get("/api/products/{productId}", 1L)
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(productResponse1.id().intValue())))
                .andExpect(jsonPath("$.name", is(productResponse1.name())))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/products/1")))
                .andExpect(jsonPath("$._links.products.href", endsWith("/api/products")))
                .andExpect(jsonPath("$._links.inventory.href", endsWith("/api/inventory/products/1")));
    }

    @Test
    void getAllProducts_shouldReturnCollectionOfProductsWithLinks() throws Exception {
        List<ProductResponse> productResponses = Arrays.asList(productResponse1, productResponse2);
        when(productCatalogService.getAllProducts()).thenReturn(CompletableFuture.completedFuture(productResponses));

        mockMvc.perform(get("/api/products")
                        .accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/products")))
                .andExpect(jsonPath("$._embedded.productResponseList.length()", is(2)))
                .andExpect(jsonPath("$._embedded.productResponseList[0].id", is(1)))
                .andExpect(jsonPath("$._embedded.productResponseList[0]._links.self.href", endsWith("/api/products/1")))
                .andExpect(jsonPath("$._embedded.productResponseList[1].id", is(2)))
                .andExpect(jsonPath("$._embedded.productResponseList[1]._links.self.href", endsWith("/api/products/2")));
    }

    @Test
    void createProduct_withAdminRole_shouldReturnCreatedProductWithLinks() throws Exception {
        setupAsAdmin();
        ProductRequest productRequest = new ProductRequest("Keyboard", "Mechanical keyboard", BigDecimal.valueOf(150.00));
        ProductResponse createdProduct = new ProductResponse(3L, "Keyboard", "Mechanical keyboard", BigDecimal.valueOf(150.00), true, LocalDateTime.now(), LocalDateTime.now());

        when(productCatalogService.createProduct(any(ProductRequest.class))).thenReturn(CompletableFuture.completedFuture(createdProduct));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/products/3")))
                .andExpect(jsonPath("$.id", is(createdProduct.id().intValue())))
                .andExpect(jsonPath("$.name", is(createdProduct.name())))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/products/3")));
    }

    @Test
    void createProduct_withCustomerRole_shouldReturnForbidden() throws Exception {
        setupAsCustomer();
        ProductRequest productRequest = new ProductRequest("Keyboard", "Mechanical keyboard", BigDecimal.valueOf(150.00));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_unauthenticated_shouldReturnUnauthorized() throws Exception {
        ProductRequest productRequest = new ProductRequest("Keyboard", "Mechanical keyboard", BigDecimal.valueOf(150.00));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProduct_withAdminRole_shouldReturnUpdatedProductWithLinks() throws Exception {
        setupAsAdmin();
        ProductRequest productRequest = new ProductRequest("Laptop", "Even more powerful laptop", BigDecimal.valueOf(1500.00));
        ProductResponse updatedProduct = new ProductResponse(1L, "Laptop", "Even more powerful laptop", BigDecimal.valueOf(1500.00), true, LocalDateTime.now(), LocalDateTime.now());

        when(productCatalogService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(CompletableFuture.completedFuture(updatedProduct));

        mockMvc.perform(put("/api/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedProduct.id().intValue())))
                .andExpect(jsonPath("$.description", is(updatedProduct.description())))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/products/1")));
    }

    @Test
    void updateProduct_withCustomerRole_shouldReturnForbidden() throws Exception {
        setupAsCustomer();
        ProductRequest productRequest = new ProductRequest("Laptop", "Even more powerful laptop", BigDecimal.valueOf(1500.00));

        mockMvc.perform(put("/api/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProduct_withAdminRole_shouldReturnNoContent() throws Exception {
        setupAsAdmin();
        when(productCatalogService.deleteProduct(anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/api/products/{productId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_withCustomerRole_shouldReturnForbidden() throws Exception {
        setupAsCustomer();
        mockMvc.perform(delete("/api/products/{productId}", 1L))
                .andExpect(status().isForbidden());
    }
}
