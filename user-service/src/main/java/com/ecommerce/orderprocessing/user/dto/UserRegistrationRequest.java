package com.ecommerce.orderprocessing.user.dto;

import jakarta.validation.constraints.*;


/**
 * Record for user registration.
 */
public record UserRegistrationRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

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
