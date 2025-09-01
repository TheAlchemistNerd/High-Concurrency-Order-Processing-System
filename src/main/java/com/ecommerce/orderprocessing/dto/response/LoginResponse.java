package com.ecommerce.orderprocessing.dto.response;

/**
 * Record for login response.
 */
public record LoginResponse(
        String token,
        String tokenType,
        Long expiresIn,
        CustomerResponse customer
) {
    // Custom constructor to set default tokenType
    public LoginResponse(String token, Long expiresIn, CustomerResponse customer) {
        this(token, "Bearer", expiresIn, customer);
    }
}