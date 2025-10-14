package com.ecommerce.orderprocessing.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider("test-secret-key-that-is-long-enough-for-hs256", 3600000L);
    }

    @Test
    void generateToken_shouldGenerateValidToken() {
        // Given
        String email = "test@test.com";
        String role = "CUSTOMER";

        // When
        String token = jwtTokenProvider.generateToken(email, role);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(email);
        assertThat(jwtTokenProvider.getRoleFromToken(token)).isEqualTo(role);
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("invalid-token");

        // Then
        assertThat(isValid).isFalse();
    }
}
