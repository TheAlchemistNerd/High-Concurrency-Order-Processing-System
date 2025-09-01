package com.ecommerce.orderprocessing.repository;

import com.ecommerce.orderprocessing.domain.entity.Product;

/**
 * A type-safe record to hold the result of best-selling product queries.
 */
record BestSellingProduct(Product product, long totalSold) {}