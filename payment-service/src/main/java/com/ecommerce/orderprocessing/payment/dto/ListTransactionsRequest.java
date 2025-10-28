package com.ecommerce.orderprocessing.payment.dto;

import java.time.LocalDateTime;

public record ListTransactionsRequest(
        String customerId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String status,
        Integer limit,
        Integer offset
) {}
