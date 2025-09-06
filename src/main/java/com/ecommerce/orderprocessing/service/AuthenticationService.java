package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.domain.enumeration.UserRole;
import com.ecommerce.orderprocessing.dto.request.CustomerRegistrationRequest;
import com.ecommerce.orderprocessing.dto.response.CustomerResponse;
import com.ecommerce.orderprocessing.dto.request.LoginRequest;
import com.ecommerce.orderprocessing.dto.response.LoginResponse;
import com.ecommerce.orderprocessing.exception.BadRequestException;
import com.ecommerce.orderprocessing.repository.CustomerRepository;
import com.ecommerce.orderprocessing.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoderService passwordEncoderService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ExecutorService virtualThreadExecutor;

    public AuthenticationService(CustomerRepository customerRepository,
                                 PasswordEncoderService passwordEncoderService,
                                 JwtTokenProvider jwtTokenProvider,
                                 ExecutorService virtualThreadExecutor) {
        this.customerRepository = customerRepository;
        this.passwordEncoderService = passwordEncoderService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public CompletableFuture<LoginResponse> authenticate(LoginRequest loginRequest) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Authenticating user: {}", loginRequest.email());

            var customer = customerRepository.findByEmail(loginRequest.email())
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            if (!customer.getIsActive()) {
                throw new BadCredentialsException("User account is deactivated");
            }

            if (!passwordEncoderService.verifyPassword(loginRequest.password(), customer.getPasswordHash())) {
                throw new BadCredentialsException("Invalid email or password");
            }

            var token = jwtTokenProvider.generateToken(customer.getEmail(), customer.getRole().toString());
            var expirationTime = jwtTokenProvider.getExpirationTime();
            var customerResponse = convertToCustomerResponse(customer);

            log.info("User authenticated successfully: {}", loginRequest.email());
            return new LoginResponse(token, expirationTime, customerResponse);
        }, virtualThreadExecutor);
    }

    @Transactional
    public CompletableFuture<CustomerResponse> register(@Valid CustomerRegistrationRequest registrationRequest) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Registering new user: {}", registrationRequest.email());

            if (customerRepository.existsByEmail(registrationRequest.email())) {
                throw new BadRequestException("Email already registered.");
            }

            Customer customer = new Customer(
                    registrationRequest.name(),
                    registrationRequest.email(),
                    passwordEncoderService.encodePassword(registrationRequest.password())
            );
            customer.setPhoneNumber(registrationRequest.phoneNumber());
            customer.setRole(UserRole.CUSTOMER); // Default role
            customer.setIsActive(true); // Default active

            var savedCustomer = customerRepository.save(customer);
            log.info("User registered successfully: {}", savedCustomer.getEmail());
            return convertToCustomerResponse(savedCustomer);
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

    private CustomerResponse convertToCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getRole().toString(),
                customer.getIsActive(),
                customer.getCreatedAt()
        );
    }
}

