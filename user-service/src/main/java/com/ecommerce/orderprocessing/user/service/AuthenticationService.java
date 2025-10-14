package com.ecommerce.orderprocessing.user.service;

import com.ecommerce.orderprocessing.common.exception.BadRequestException;
import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.user.domain.entity.Role;
import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.user.dto.LoginRequest;
import com.ecommerce.orderprocessing.user.dto.LoginResponse;
import com.ecommerce.orderprocessing.user.dto.UserRegistrationRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.repository.RoleRepository;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import com.ecommerce.orderprocessing.user.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoderService passwordEncoderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ExecutorService virtualThreadExecutor;

    public AuthenticationService(UserRepository userRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoderService passwordEncoderService,
                                 JwtTokenProvider jwtTokenProvider,
                                 ExecutorService virtualThreadExecutor) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoderService = passwordEncoderService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public CompletableFuture<LoginResponse> authenticate(LoginRequest loginRequest) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Authenticating user: {}", loginRequest.email());

            var user = userRepository.findByEmail(loginRequest.email())
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            if (!user.getIsActive()) {
                throw new BadCredentialsException("User account is deactivated");
            }

            if (!passwordEncoderService.verifyPassword(loginRequest.password(), user.getPasswordHash())) {
                throw new BadCredentialsException("Invalid email or password");
            }

            var token = jwtTokenProvider.generateToken(user.getEmail(),
                    user.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")));
            var expirationTime = jwtTokenProvider.getExpirationTime();
            var userResponse = convertToUserResponse(user);

            log.info("User authenticated successfully: {}", loginRequest.email());
            return new LoginResponse(token, expirationTime, userResponse);
        }, virtualThreadExecutor);
    }

    @Transactional
    public CompletableFuture<UserResponse> register(@Valid UserRegistrationRequest registrationRequest) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Registering new user: {}", registrationRequest.email());

            if (userRepository.existsByEmail(registrationRequest.email())) {
                throw new BadRequestException("Email already registered.");
            }

            User user = new User(
                    registrationRequest.firstName(),
                    registrationRequest.lastName(),
                    registrationRequest.email(),
                    passwordEncoderService.encodePassword(registrationRequest.password())
            );
            user.setPhoneNumber(registrationRequest.phoneNumber());
            user.setIsActive(true); // Default active

            // Assign default role ROLE_CUSTOMER
            Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_CUSTOMER not found"));
            user.getRoles().add(customerRole);

            var savedUser = userRepository.save(user);
            log.info("User registered successfully: {}", savedUser.getEmail());
            return convertToUserResponse(savedUser);
        }, virtualThreadExecutor);
    }

    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return jwtTokenProvider.getUsernameFromToken(token);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfilePictureUrl(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }

}

