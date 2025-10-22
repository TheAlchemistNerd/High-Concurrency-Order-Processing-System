package com.ecommerce.orderprocessing.order.repository;

import com.ecommerce.orderprocessing.product.Product;

/**
 * A type-safe record to hold the result of best-selling product queries.
 */
public record BestSellingProduct(Long productId, long totalSold) {}