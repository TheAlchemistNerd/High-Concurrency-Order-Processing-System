package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.dto.request.CustomerRegistrationRequest;
import com.ecommerce.orderprocessing.dto.request.LoginRequest;
import com.ecommerce.orderprocessing.dto.response.CustomerResponse;
import com.ecommerce.orderprocessing.dto.response.LoginResponse;
import com.ecommerce.orderprocessing.exception.BadRequestException;
import com.ecommerce.orderprocessing.repository.CustomerRepository;
import com.ecommerce.orderprocessing.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoderService passwordEncoderService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(customerRepository, passwordEncoderService, jwtTokenProvider, virtualThreadExecutor);
    }

    @Test
    void authenticate_shouldReturnLoginResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password");
        Customer customer = new Customer("Test Customer", "test@test.com", "encodedPassword");
        when(customerRepository.findByEmail("test@test.com")).thenReturn(Optional.of(customer));
        when(passwordEncoderService.verifyPassword("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(), any())).thenReturn("token");

        // When
        CompletableFuture<LoginResponse> future = authenticationService.authenticate(loginRequest);
        LoginResponse loginResponse = future.get();

        // Then
        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.token()).isEqualTo("token");
    }

    @Test
    void authenticate_whenInvalidCredentials_shouldThrowException() {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password");
        when(customerRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // When
        CompletableFuture<LoginResponse> future = authenticationService.authenticate(loginRequest);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(BadCredentialsException.class);
    }

    @Test
    void register_shouldReturnCustomerResponse() throws Exception {
        // Given
        CustomerRegistrationRequest registrationRequest = new CustomerRegistrationRequest("Test Customer", "test@test.com", "password", "1234567890");
        when(customerRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoderService.encodePassword("password")).thenReturn("encodedPassword");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CompletableFuture<CustomerResponse> future = authenticationService.register(registrationRequest);
        CustomerResponse customerResponse = future.get();

        // Then
        assertThat(customerResponse).isNotNull();
        assertThat(customerResponse.email()).isEqualTo("test@test.com");
    }

    @Test
    void register_whenEmailExists_shouldThrowException() {
        // Given
        CustomerRegistrationRequest registrationRequest = new CustomerRegistrationRequest("Test Customer", "test@test.com", "password", "1234567890");
        when(customerRepository.existsByEmail("test@test.com")).thenReturn(true);

        // When
        CompletableFuture<CustomerResponse> future = authenticationService.register(registrationRequest);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(BadRequestException.class);
    }
}
