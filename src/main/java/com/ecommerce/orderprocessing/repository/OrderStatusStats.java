package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.enumeration.OrderStatus;

/**
 * A type-safe record to hold the result of order statistics queries.
 */
record OrderStatusStats(OrderStatus status, long count) {}
