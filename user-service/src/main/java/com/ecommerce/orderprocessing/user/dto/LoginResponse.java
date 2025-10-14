package com.ecommerce.orderprocessing.user.dto;

/**
 * Record for login response.
 */
public record LoginResponse(
        String token,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
    // Custom constructor to set default tokenType
    public LoginResponse(String token, Long expiresIn, UserResponse user) {
        this(token, "Bearer", expiresIn, user);
    }
}