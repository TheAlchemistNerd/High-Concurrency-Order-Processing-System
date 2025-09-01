package com.ecommerce.orderprocessing.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Record for customer response.
 */
public record CustomerResponse(
        Long id,
        String name,
        String email,
        String phoneNumber,
        String role,
        Boolean isActive,
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
