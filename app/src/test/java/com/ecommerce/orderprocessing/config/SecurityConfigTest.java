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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;
import com.ecommerce.orderprocessing.user.security.oauth2.CustomOAuth2UserService;
import com.ecommerce.orderprocessing.user.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.ecommerce.orderprocessing.order.controller.OrderModelAssembler;
import com.ecommerce.orderprocessing.order.controller.PaymentModelAssembler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = {AuthController.class, OrderController.class})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockitoBean
    private OrderModelAssembler orderModelAssembler;

    @MockitoBean
    private PaymentModelAssembler paymentModelAssembler;

    @Test
    void whenUnauthenticated_thenPublicEndpointsAreAccessible() throws Exception {
        int status = mockMvc.perform(get("/api/auth/login"))
                .andReturn().getResponse().getStatus();
        System.out.println("Actual Status for /api/auth/login (GET): " + status);
        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }
        
            @Test
            void whenUnauthenticated_thenSecureEndpointsAreInaccessible() throws Exception {
                mockMvc.perform(get("/api/orders"))
                        .andDo(print())
                        .andExpect(status().isUnauthorized());
            }
        
            @Test
            @WithMockUser(roles = "CUSTOMER")
            void whenAuthenticatedAsCustomer_thenCustomerEndpointsAreAccessible() throws Exception {
                mockMvc.perform(get("/api/orders/customer/1"))
                        .andDo(print())
                        .andExpect(status().isNotFound()); // Expecting 404 because no data is mocked
            }
        
            @Test
            @WithMockUser(roles = "CUSTOMER")
            void whenAuthenticatedAsCustomer_thenAdminEndpointsAreInaccessible() throws Exception {
                mockMvc.perform(get("/api/orders/admin/all"))
                        .andDo(print())
                        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAuthenticatedAsAdmin_thenAdminEndpointsAreAccessible() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}