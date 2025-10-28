# Product Service Documentation

## 1. Introduction

The Product Service is responsible for managing the product catalog of the e-commerce platform. It serves as the single, authoritative source of truth for all descriptive product information, such as product names, descriptions, prices, and active status. This service supports the full lifecycle of product data, from creation to deletion.

## 2. Key Features

*   **Product Catalog Management**: Provides CRUD (Create, Read, Update, Delete) operations for products.
*   **Product Information Retrieval**: Allows for fetching individual product details or a list of all products.
*   **Role-Based Access Control (RBAC)**: Secures administrative operations to `ADMIN` and `PRODUCT_MANAGER` roles.

## 3. API Endpoints

All API endpoints are prefixed with `/api/products`.

#### `GET /api/products/{productId}`
*   **Description**: Retrieves detailed information for a single product by its ID.
*   **Authentication**: Not required (Anonymous access).
*   **Authorization**: None.
*   **Path Variable**: `productId` (Long) - The unique identifier of the product.
*   **Response**: `200 OK` with `ProductResponse` body.

#### `GET /api/products`
*   **Description**: Retrieves a list of all products in the catalog.
*   **Authentication**: Not required (Anonymous access).
*   **Authorization**: None.
*   **Response**: `200 OK` with a list of `ProductResponse` bodies.

#### `POST /api/products`
*   **Description**: Creates a new product in the catalog.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_PRODUCT_MANAGER`.
*   **Request Body**: `ProductRequest`
*   **Response**: `201 Created` with `ProductResponse` body.

#### `PUT /api/products/{productId}`
*   **Description**: Updates an existing product identified by its ID.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_PRODUCT_MANAGER`.
*   **Path Variable**: `productId` (Long) - The unique identifier of the product to update.
*   **Request Body**: `ProductRequest`
*   **Response**: `200 OK` with `ProductResponse` body.

#### `DELETE /api/products/{productId}`
*   **Description**: Deletes a product from the catalog by its ID.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_PRODUCT_MANAGER`.
*   **Path Variable**: `productId` (Long) - The unique identifier of the product to delete.
*   **Response**: `204 No Content`.

## 4. Data Transfer Objects (DTOs)

### `ProductResponse`
Represents the detailed information of a product.
*   `id`: Long - Unique identifier of the product.
*   `name`: String - Name of the product.
*   `description`: String - Description of the product.
*   `price`: BigDecimal - Price of the product.
*   `isActive`: Boolean - Indicates if the product is active and available.
*   `createdAt`: LocalDateTime - Timestamp of when the product was created.
*   `updatedAt`: LocalDateTime - Timestamp of the last update to the product.

### `ProductRequest`
Request body for creating or updating a product.
*   `name`: String - Name of the product.
*   `description`: String - Description of the product.
*   `price`: BigDecimal - Price of the product.
