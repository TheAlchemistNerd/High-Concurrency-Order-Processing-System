package com.ecommerce.orderprocessing.product.controller;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductResponse productResponse1;
    private ProductResponse productResponse2;

    @BeforeEach
    void setUp() {
        productResponse1 = new ProductResponse(1L, "Laptop", "Powerful laptop", BigDecimal.valueOf(1200.00), true, LocalDateTime.now(), LocalDateTime.now());
        productResponse2 = new ProductResponse(2L, "Mouse", "Wireless mouse", BigDecimal.valueOf(25.00), true, LocalDateTime.now(), LocalDateTime.now());

        AppUserDetails adminUserDetails = new AppUserDetails(1L, "admin@example.com", "password", "ADMIN", true, Collections.emptyMap());
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuthentication);
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

    void createProduct_withAdminRole_shouldReturnCreatedProduct() throws Exception {
        ProductRequest productRequest = new ProductRequest("Keyboard", "Mechanical keyboard", BigDecimal.valueOf(150.00));
        ProductResponse createdProduct = new ProductResponse(3L, "Keyboard", "Mechanical keyboard", BigDecimal.valueOf(150.00), true, LocalDateTime.now(), LocalDateTime.now());

        when(productCatalogService.createProduct(any(ProductRequest.class))).thenReturn(CompletableFuture.completedFuture(createdProduct));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(".id").value(createdProduct.id()))
                .andExpect(jsonPath(".name").value(createdProduct.name()));
    }

    @Test
    void createProduct_withCustomerRole_shouldReturnForbidden() throws Exception {
        AppUserDetails customerUserDetails = new AppUserDetails(2L, "customer@example.com", "password", "CUSTOMER", true, Collections.emptyMap());
        Authentication customerAuthentication = new UsernamePasswordAuthenticationToken(
                customerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(customerAuthentication);

        ProductRequest productRequest = new ProductRequest("Keyboard", "Mechanical keyboard", BigDecimal.valueOf(150.00));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_unauthenticated_shouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        ProductRequest productRequest = new ProductRequest("Keyboard", "Mechanical keyboard", BigDecimal.valueOf(150.00));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProduct_withAdminRole_shouldReturnUpdatedProduct() throws Exception {
        ProductRequest productRequest = new ProductRequest("Laptop", "Even more powerful laptop", BigDecimal.valueOf(1500.00));
        ProductResponse updatedProduct = new ProductResponse(1L, "Laptop", "Even more powerful laptop", BigDecimal.valueOf(1500.00), true, LocalDateTime.now(), LocalDateTime.now());

        when(productCatalogService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(CompletableFuture.completedFuture(updatedProduct));

        mockMvc.perform(put("/api/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(updatedProduct.id()))
                .andExpect(jsonPath(".description").value(updatedProduct.description()));
    }

    @Test
    void updateProduct_withCustomerRole_shouldReturnForbidden() throws Exception {
        AppUserDetails customerUserDetails = new AppUserDetails(2L, "customer@example.com", "password", "CUSTOMER", true, Collections.emptyMap());
        Authentication customerAuthentication = new UsernamePasswordAuthenticationToken(
                customerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(customerAuthentication);

        ProductRequest productRequest = new ProductRequest("Laptop", "Even more powerful laptop", BigDecimal.valueOf(1500.00));

        mockMvc.perform(put("/api/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProduct_withAdminRole_shouldReturnNoContent() throws Exception {
        when(productCatalogService.deleteProduct(anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/api/products/{productId}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_withCustomerRole_shouldReturnForbidden() throws Exception {
        AppUserDetails customerUserDetails = new AppUserDetails(2L, "customer@example.com", "password", "CUSTOMER", true, Collections.emptyMap());
        Authentication customerAuthentication = new UsernamePasswordAuthenticationToken(
                customerUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(customerAuthentication);

        mockMvc.perform(delete("/api/products/{productId}", 1L))
                .andExpect(status().isForbidden());
    }
}
}