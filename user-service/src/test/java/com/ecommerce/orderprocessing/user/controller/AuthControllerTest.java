package com.ecommerce.orderprocessing.user.controller;

import com.ecommerce.orderprocessing.user.dto.LoginRequest;
import com.ecommerce.orderprocessing.user.dto.LoginResponse;
import com.ecommerce.orderprocessing.user.dto.UserRegistrationRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;
import com.ecommerce.orderprocessing.user.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void login_shouldReturnToken() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password");
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", "http://example.com/pic.jpg", "ROLE_CUSTOMER", true, LocalDateTime.now());
        LoginResponse loginResponse = new LoginResponse("token", 3600L, userResponse);
        when(authenticationService.authenticate(any(LoginRequest.class))).thenReturn(CompletableFuture.completedFuture(loginResponse));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"test@test.com\", \"password\": \"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.user.email").value("test@test.com"));
    }

    @Test
    void register_shouldReturnUserResponse() throws Exception {
        // Given
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest("Test", "Customer", "test@test.com", "password", "1234567890");
        UserResponse userResponse = new UserResponse(1L, "Test", "Customer", "test@test.com", "1234567890", "http://example.com/pic.jpg", "ROLE_CUSTOMER", true, LocalDateTime.now());
        when(authenticationService.register(any(UserRegistrationRequest.class))).thenReturn(CompletableFuture.completedFuture(userResponse));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"Test\", \"lastName\": \"Customer\", \"email\": \"test@test.com\", \"password\": \"password\", \"phoneNumber\": \"1234567890\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }
}