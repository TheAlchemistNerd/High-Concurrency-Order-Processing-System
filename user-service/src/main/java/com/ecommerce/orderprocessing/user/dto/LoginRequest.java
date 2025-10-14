package com.ecommerce.orderprocessing.user.dto;

import jakarta.validation.constraints.*;

import jakarta.validation.constraints.*;

/**
 * Record for user login.
 */
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        String password
) {}
