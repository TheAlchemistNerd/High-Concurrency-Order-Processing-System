package com.ecommerce.orderprocessing.config;

import com.ecommerce.orderprocessing.user.controller.AuthController;
import com.ecommerce.orderprocessing.order.controller.OrderController;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import com.ecommerce.orderprocessing.user.security.JwtTokenProvider;
import com.ecommerce.orderprocessing.user.service.AuthenticationService;
import com.ecommerce.orderprocessing.user.service.CustomUserDetailsService;
import com.ecommerce.orderprocessing.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {AuthController.class, OrderController.class})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void whenUnauthenticated_thenPublicEndpointsAreAccessible() throws Exception {
                mockMvc.perform(get("/api/auth/login"))
                        .andExpect(status().isMethodNotAllowed());
        
            }
        
            @Test
            void whenUnauthenticated_thenSecureEndpointsAreInaccessible() throws Exception {
                mockMvc.perform(get("/api/orders"))
                        .andExpect(status().isUnauthorized());
            }
        
            @Test
            @WithMockUser(roles = "CUSTOMER")
            void whenAuthenticatedAsCustomer_thenCustomerEndpointsAreAccessible() throws Exception {
                mockMvc.perform(get("/api/orders/customer/1"))
                        .andExpect(status().isNotFound()); // Expecting 404 because no data is mocked
            }
        
            @Test
            @WithMockUser(roles = "CUSTOMER")
            void whenAuthenticatedAsCustomer_thenAdminEndpointsAreInaccessible() throws Exception {
                mockMvc.perform(get("/api/orders/admin/all"))
                        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAuthenticatedAsAdmin_thenAdminEndpointsAreAccessible() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());
    }
}