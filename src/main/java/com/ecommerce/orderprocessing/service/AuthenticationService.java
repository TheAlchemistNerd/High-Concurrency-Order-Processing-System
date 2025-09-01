package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.dto.response.CustomerResponse;
import com.ecommerce.orderprocessing.dto.request.LoginRequest;
import com.ecommerce.orderprocessing.dto.response.LoginResponse;
import com.ecommerce.orderprocessing.repository.CustomerRepository;
import com.ecommerce.orderprocessing.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

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
            logger.info("Authenticating user: {}", loginRequest.email());

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

            logger.info("User authenticated successfully: {}", loginRequest.email());
            return new LoginResponse(token, expirationTime, customerResponse);
        }, virtualThreadExecutor);
    }

    public boolean validateToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
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

