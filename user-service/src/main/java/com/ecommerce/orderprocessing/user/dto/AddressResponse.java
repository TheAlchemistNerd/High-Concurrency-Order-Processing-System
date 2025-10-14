package com.ecommerce.orderprocessing.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Record for address response.
 */
public record AddressResponse(
        Long id,
        String street,
        String city,
        String state,
        String postalCode,
        String country,
        Boolean isDefaultShipping,
        Boolean isDefaultBilling,
        @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
