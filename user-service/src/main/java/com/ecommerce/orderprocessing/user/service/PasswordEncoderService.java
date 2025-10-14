package com.ecommerce.orderprocessing.user.service;

import com.ecommerce.orderprocessing.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service providing a façade over the Spring Security PasswordEncoder.
 */
@Service
@Slf4j
public class PasswordEncoderService {

    private final PasswordEncoder passwordEncoder;

    public PasswordEncoderService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new BadRequestException("Password cannot be null or empty");
        }
        var encodedPassword = passwordEncoder.encode(rawPassword);
        log.debug("Password encoded successfully");
        return encodedPassword;
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        log.debug("Password verification result: {}", matches);
        return matches;
    }
}

