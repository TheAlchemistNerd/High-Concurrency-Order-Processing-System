package com.ecommerce.orderprocessing.config;

import com.ecommerce.orderprocessing.common.AbstractContainerBaseTest;
import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.user.domain.entity.Role;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import com.ecommerce.orderprocessing.user.repository.RoleRepository;
import com.ecommerce.orderprocessing.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class SecurityConfigIntegrationTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String customerToken;
    private String adminToken;
    private Role customerRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // Ensure roles exist
        customerRole = roleRepository.findByName("CUSTOMER").orElseGet(() -> roleRepository.save(new Role("CUSTOMER")));
        adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role("ADMIN")));

        // Create a customer user
        User customer = new User("customer", "Customer", "customer@example.com", passwordEncoder.encode("password"));
        customer.setRoles(Set.of(customerRole));
        userRepository.save(customer);
        customerToken = jwtTokenProvider.generateToken(customer.getEmail(), customerRole.getName());

        // Create an admin user
        User admin = new User("admin", "Admin", "admin@example.com", passwordEncoder.encode("password"));
        admin.setRoles(Set.of(adminRole));
        userRepository.save(admin);
        adminToken = jwtTokenProvider.generateToken(admin.getEmail(), adminRole.getName());
    }

    @Test
    void publicEndpoint_shouldPermitAll() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void customerEndpoint_shouldBeAccessibleByCustomer() throws Exception {
        mockMvc.perform(get("/api/orders/customer/1")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());
    }

    @Test
    void customerEndpoint_shouldBeForbiddenByAdmin() throws Exception {
        mockMvc.perform(get("/api/orders/customer/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_shouldBeAccessibleByAdmin() throws Exception {
        mockMvc.perform(get("/api/orders/admin/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_shouldBeForbiddenByCustomer() throws Exception {
        mockMvc.perform(get("/api/orders/admin/1")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_shouldBeUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_shouldBeUnauthorizedWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/orders/customer/1")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postCustomerOrder_shouldBeAccessibleByCustomer() throws Exception {
        String orderJson = "{\"productId\": 1, \"quantity\": 1}"; // Simplified for test
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isOk()); // Assuming 200 OK for successful order creation
    }

    @Test
    void putAdminOrderStatus_shouldBeAccessibleByAdmin() throws Exception {
        String statusUpdateJson = "{\"status\": \"SHIPPED\"}"; // Simplified for test
        mockMvc.perform(put("/api/orders/1/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusUpdateJson))
                .andExpect(status().isOk()); // Assuming 200 OK for successful status update
    }

    @Test
    void deleteAdminOrder_shouldBeAccessibleByAdmin() throws Exception {
        mockMvc.perform(delete("/api/orders/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}