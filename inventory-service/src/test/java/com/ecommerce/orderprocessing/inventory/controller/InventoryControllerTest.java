package com.ecommerce.orderprocessing.inventory.controller;

import com.ecommerce.orderprocessing.inventory.dto.InventoryResponse;
import com.ecommerce.orderprocessing.inventory.service.InventoryService;
import com.ecommerce.orderprocessing.product.controller.ProductController;
import com.ecommerce.orderprocessing.product.service.ProductCatalogService;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(InventoryController.class)
@Import({InventoryModelAssembler.class, ProductController.class})
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private ProductCatalogService productCatalogService; // For ProductController link building

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    private void setupAs(String role) {
        AppUserDetails userDetails = new AppUserDetails(1L, "user@example.com", "password", role, true, Collections.emptyMap());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getInventoryByProductId_shouldReturnInventoryWithLinks() throws Exception {
        InventoryResponse inventoryResponse = new InventoryResponse(1L, 100);
        when(inventoryService.getInventoryByProductId(1L)).thenReturn(CompletableFuture.completedFuture(inventoryResponse));

        mockMvc.perform(get("/api/inventory/products/{productId}", 1L).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.stockQuantity", is(100)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/inventory/products/1")))
                .andExpect(jsonPath("$._links.product.href", endsWith("/api/products/1")))
                .andExpect(jsonPath("$._links.check.href", endsWith("/api/inventory/products/1/check")))
                .andExpect(jsonPath("$._links.reserve.href", endsWith("/api/inventory/products/1/reserve")));
    }

    @Test
    void checkInventory_shouldReturnBoolean() throws Exception {
        when(inventoryService.checkInventory(1L, 10)).thenReturn(CompletableFuture.completedFuture(true));

        mockMvc.perform(get("/api/inventory/products/{productId}/check", 1L).param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void reserveInventory_withOrderManagerRole_shouldReturnOk() throws Exception {
        setupAs("ORDER_MANAGER");
        when(inventoryService.reserveInventory(1L, 5)).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/inventory/products/{productId}/reserve", 1L).param("quantity", "5"))
                .andExpect(status().isOk());
    }

    @Test
    void reserveInventory_withCustomerRole_shouldReturnForbidden() throws Exception {
        setupAs("CUSTOMER");
        mockMvc.perform(post("/api/inventory/products/{productId}/reserve", 1L).param("quantity", "5"))
                .andExpect(status().isForbidden());
    }

    @Test
    void restockInventory_withAdminRole_shouldReturnOk() throws Exception {
        setupAs("ADMIN");
        when(inventoryService.restockInventory(1L, 50)).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/inventory/products/{productId}/restock", 1L).param("quantity", "50"))
                .andExpect(status().isOk());
    }

    @Test
    void restockInventory_withProductManagerRole_shouldReturnOk() throws Exception {
        setupAs("PRODUCT_MANAGER");
        when(inventoryService.restockInventory(1L, 50)).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/inventory/products/{productId}/restock", 1L).param("quantity", "50"))
                .andExpect(status().isOk());
    }

    @Test
    void restockInventory_withOrderManagerRole_shouldReturnForbidden() throws Exception {
        setupAs("ORDER_MANAGER");
        mockMvc.perform(post("/api/inventory/products/{productId}/restock", 1L).param("quantity", "50"))
                .andExpect(status().isForbidden());
    }

    @Test
    void restockInventory_unauthenticated_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/inventory/products/{productId}/restock", 1L).param("quantity", "50"))
                .andExpect(status().isUnauthorized());
    }
}