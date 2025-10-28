package com.ecommerce.orderprocessing.payment.dto;

public record PaymentMethodResponse(
        String paymentMethodId,
        String customerId,
        String type,
        String lastFour,
        String brand,
        Integer expMonth,
        Integer expYear,
        boolean isDefault,
        String message
) {}
