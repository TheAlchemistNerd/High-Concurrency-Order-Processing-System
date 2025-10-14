package com.ecommerce.orderprocessing.user.service;

import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.user.dto.UserRegistrationRequest;
import com.ecommerce.orderprocessing.user.dto.LoginRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.dto.LoginResponse;
import com.ecommerce.orderprocessing.common.exception.BadRequestException;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import com.ecommerce.orderprocessing.user.domain.entity.Role;
import com.ecommerce.orderprocessing.user.repository.RoleRepository;
import com.ecommerce.orderprocessing.user.security.JwtTokenProvider;
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
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoderService passwordEncoderService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userRepository, roleRepository, passwordEncoderService, jwtTokenProvider, virtualThreadExecutor);
    }

    @Test
    void authenticate_shouldReturnLoginResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("test@test.com", "password");
        User user = new User("Test", "User", "test@test.com", "encodedPassword");
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
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
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // When
        CompletableFuture<LoginResponse> future = authenticationService.authenticate(loginRequest);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(BadCredentialsException.class);
    }

    @Test
    void register_shouldReturnUserResponse() throws Exception {
        // Given
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest("Test", "User", "test@test.com", "password", "1234567890");
        Role customerRole = new Role("ROLE_CUSTOMER");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoderService.encodePassword("password")).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L); // Simulate ID generation
            return user;
        });

        // When
        CompletableFuture<UserResponse> future = authenticationService.register(registrationRequest);
        UserResponse userResponse = future.get();

        // Then
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.email()).isEqualTo("test@test.com");
        assertThat(userResponse.roles()).contains("ROLE_CUSTOMER");
    }

    @Test
    void register_whenEmailExists_shouldThrowException() {
        // Given
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest("Test", "User", "test@test.com", "password", "1234567890");
        when(userRepository.existsByEmail(registrationRequest.email())).thenReturn(true);

        // When
        CompletableFuture<UserResponse> future = authenticationService.register(registrationRequest);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(BadRequestException.class);
    }
}
