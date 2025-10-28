# Inventory Service Documentation

## 1. Introduction

The Inventory Service is a critical component responsible for managing the stock levels of products within the e-commerce platform. It provides a transactional and high-performance ledger for product stock, ensuring accurate availability information and preventing overselling. This service is designed to handle highly concurrent requests related to inventory checks, reservations, releases, and commitments.

## 2. Key Features

*   **Real-time Stock Check**: Provides immediate information on product availability.
*   **Transactional Inventory Operations**: Supports atomic operations for reserving, releasing, and committing stock, crucial for order processing.
*   **Restock Management**: Allows for updating stock levels due to new shipments or returns.
*   **Role-Based Access Control (RBAC)**: Secures sensitive inventory modification operations.

## 3. API Endpoints

All API endpoints are prefixed with `/api/inventory`.

#### `GET /api/inventory/products/{productId}/check`
*   **Description**: Checks if a specified quantity of a product is available in stock.
*   **Authentication**: Not required (Anonymous access).
*   **Authorization**: None.
*   **Path Variable**: `productId` (Long) - The unique identifier of the product.
*   **Query Parameter**: `quantity` (Integer) - The quantity to check for availability.
*   **Response**: `200 OK` with a `Boolean` indicating availability (`true` if available, `false` otherwise).

#### `GET /api/inventory/products/{productId}`
*   **Description**: Retrieves the current stock quantity for a specific product.
*   **Authentication**: Not required (Anonymous access).
*   **Authorization**: None.
*   **Path Variable**: `productId` (Long) - The unique identifier of the product.
*   **Response**: `200 OK` with `InventoryResponse` body.

#### `POST /api/inventory/products/{productId}/reserve`
*   **Description**: Reserves a specified quantity of a product. This operation is typically called by the Order Service during order creation.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ORDER_MANAGER` (Internal service-to-service call).
*   **Path Variable**: `productId` (Long) - The unique identifier of the product.
*   **Query Parameter**: `quantity` (Integer) - The quantity to reserve.
*   **Response**: `200 OK` (No content, or success indicator).

#### `POST /api/inventory/products/{productId}/release`
*   **Description**: Releases a previously reserved quantity of a product. This is used for scenarios like cart expiration or payment failure.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ORDER_MANAGER` (Internal service-to-service call).
*   **Path Variable**: `productId` (Long) - The unique identifier of the product.
*   **Query Parameter**: `quantity` (Integer) - The quantity to release.
*   **Response**: `200 OK` (No content, or success indicator).

#### `POST /api/inventory/products/{productId}/commit`
*   **Description**: Commits a stock deduction after a successful sale. This reduces the actual stock level.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ORDER_MANAGER` (Internal service-to-service call).
*   **Path Variable**: `productId` (Long) - The unique identifier of the product.
*   **Query Parameter**: `quantity` (Integer) - The quantity to commit.
*   **Response**: `200 OK` (No content, or success indicator).

#### `POST /api/inventory/products/{productId}/restock`
*   **Description**: Adds a specified quantity to the stock level of a product. Used for new shipments or processing returns.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_PRODUCT_MANAGER`.
*   **Path Variable**: `productId` (Long) - The unique identifier of the product.
*   **Query Parameter**: `quantity` (Integer) - The quantity to add to stock.
*   **Response**: `200 OK` (No content, or success indicator).

## 4. Data Transfer Objects (DTOs)

### `InventoryResponse`
Represents the stock information for a product.
*   `productId`: Long - The unique identifier of the product.
*   `stockQuantity`: Integer - The current available stock quantity for the product.
