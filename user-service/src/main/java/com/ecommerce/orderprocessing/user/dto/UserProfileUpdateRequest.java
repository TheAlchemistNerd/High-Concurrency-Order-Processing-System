package com.ecommerce.orderprocessing.user.dto;

import jakarta.validation.constraints.Size;

/**
 * Record for user profile update request.
 */
public record UserProfileUpdateRequest(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,

        @Size(max = 255, message = "Profile picture URL must not exceed 255 characters")
        String profilePictureUrl
) {}
