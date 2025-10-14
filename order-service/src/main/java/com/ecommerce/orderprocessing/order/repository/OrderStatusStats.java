package com.ecommerce.orderprocessing.order.repository;

import com.ecommerce.orderprocessing.order.domain.enumeration.OrderStatus;

/**
 * A type-safe record to hold the result of order statistics queries.
 */
record OrderStatusStats(OrderStatus status, long count) {}
