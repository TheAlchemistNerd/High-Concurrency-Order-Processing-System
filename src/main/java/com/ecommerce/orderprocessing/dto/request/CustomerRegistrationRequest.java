package com.ecommerce.orderprocessing.dto.request;

import jakarta.validation.constraints.*;

/**
 * Record for customer registration.
 */
public record CustomerRegistrationRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must not exceed 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
                message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit")
        String password,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        @Pattern(regexp = "^[+]?[0-9\\-\\s()]*$", message = "Phone number format is invalid")
        String phoneNumber
) {}
