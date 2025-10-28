# Shopping Cart Service Documentation

## 1. Introduction

The Shopping Cart Service is responsible for managing the state of users' shopping carts. It allows authenticated users to add, update, and remove items from their cart before proceeding to checkout. This service ensures that cart contents are persistent across user sessions.

## 2. Key Features

*   **Cart Management**: Provides functionality to retrieve, add items to, update item quantities in, and remove items from a user's shopping cart.
*   **Cart Persistence**: Ensures that shopping cart contents are saved for authenticated users.
*   **Role-Based Access Control (RBAC)**: Restricts cart operations to the owner of the cart.

## 3. API Endpoints

All API endpoints are prefixed with `/api/cart`.

#### `GET /api/cart/{customerId}`
*   **Description**: Retrieves the shopping cart for a specific customer.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER` (only the customer who owns the cart can access it).
*   **Path Variable**: `customerId` (Long) - The ID of the customer whose cart is to be retrieved.
*   **Response**: `200 OK` with `ShoppingCartResponse` body.

#### `POST /api/cart/{customerId}/items`
*   **Description**: Adds a new item to the customer's shopping cart. If the item already exists, its quantity will be updated.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER` (only the customer who owns the cart can modify it).
*   **Path Variable**: `customerId` (Long) - The ID of the customer whose cart is to be modified.
*   **Request Body**: `AddCartItemRequest`
*   **Response**: `200 OK` with `ShoppingCartResponse` body (representing the updated cart).

#### `PUT /api/cart/{customerId}/items/{productId}`
*   **Description**: Updates the quantity of a specific item in the customer's shopping cart.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER` (only the customer who owns the cart can modify it).
*   **Path Variable**: `customerId` (Long) - The ID of the customer whose cart is to be modified.
*   **Path Variable**: `productId` (Long) - The ID of the product whose quantity is to be updated.
*   **Request Body**: `UpdateCartItemRequest`
*   **Response**: `200 OK` with `ShoppingCartResponse` body (representing the updated cart).

#### `DELETE /api/cart/{customerId}/items/{productId}`
*   **Description**: Removes a specific item from the customer's shopping cart.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER` (only the customer who owns the cart can modify it).
*   **Path Variable**: `customerId` (Long) - The ID of the customer whose cart is to be modified.
*   **Path Variable**: `productId` (Long) - The ID of the product to remove.
*   **Response**: `200 OK` with `ShoppingCartResponse` body (representing the updated cart).

#### `DELETE /api/cart/{customerId}`
*   **Description**: Clears all items from the customer's shopping cart.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER` (only the customer who owns the cart can clear it).
*   **Path Variable**: `customerId` (Long) - The ID of the customer whose cart is to be cleared.
*   **Response**: `204 No Content`.

## 4. Data Transfer Objects (DTOs)

### `ShoppingCartResponse`
Represents the detailed information of a user's shopping cart.
*   `id`: Long - Unique identifier of the shopping cart.
*   `customerId`: Long - The ID of the customer who owns the cart.
*   `customerName`: String - The name of the customer.
*   `customerEmail`: String - The email of the customer.
*   `items`: List of `CartItemResponse` - A list of items currently in the cart.
*   `totalAmount`: BigDecimal - The total monetary value of all items in the cart.
*   `createdAt`: LocalDateTime - Timestamp of when the cart was created.
*   `updatedAt`: LocalDateTime - Timestamp of the last update to the cart.

### `CartItemResponse`
Represents a single item within a shopping cart.
*   `productId`: Long - The unique identifier of the product.
*   `productName`: String - The name of the product.
*   `quantity`: Integer - The quantity of the product in the cart.
*   `unitPrice`: BigDecimal - The price per unit of the product.
*   `subtotal`: BigDecimal - The total price for this item (quantity * unitPrice).

### `AddCartItemRequest`
Request body for adding an item to the cart.
*   `productId`: Long - The ID of the product to add.
*   `quantity`: Integer - The quantity of the product to add.

### `UpdateCartItemRequest`
Request body for updating the quantity of an item in the cart.
*   `quantity`: Integer - The new quantity for the product.
