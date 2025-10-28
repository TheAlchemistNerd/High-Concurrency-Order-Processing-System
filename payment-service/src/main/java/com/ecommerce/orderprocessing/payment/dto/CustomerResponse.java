package com.ecommerce.orderprocessing.payment.dto;

public record CustomerResponse(
        String customerId,
        String gatewayCustomerId,
        String name,
        String email,
        String message
) {}
