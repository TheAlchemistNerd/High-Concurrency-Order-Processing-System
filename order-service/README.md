# Order Service Documentation

## 1. Introduction

The Order Service is the central orchestrator for the entire order lifecycle within the e-commerce platform. It manages the creation, processing, and status updates of customer orders, coordinating with various other services like Inventory, User, and Payment to ensure a seamless and consistent transaction flow. This service is designed to handle high concurrency and maintain data integrity throughout the complex order process.

## 2. Key Features

*   **Order Creation**: Orchestrates the creation of new orders, including inventory reservation and initial payment processing.
*   **Order Status Management**: Tracks and updates the status of orders through their lifecycle (e.g., PENDING, PAID, SHIPPED, CANCELLED).
*   **Order History**: Provides customers with access to their order history and allows administrative/support staff to view all orders.
*   **Order Cancellation**: Handles order cancellations, including compensating actions like inventory release and payment refunds.
*   **Payment Integration**: Initiates payment processing for orders via the Payment Service.
*   **Role-Based Access Control (RBAC)**: Implements granular access control for order-related operations.

## 3. API Endpoints

All API endpoints are prefixed with `/api/orders`.

#### `POST /api/orders`
*   **Description**: Creates a new order. This involves reserving inventory and initiating payment.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Request Body**: `CreateOrderRequest`
*   **Response**: `200 OK` with `OrderResponse` body.

#### `GET /api/orders/{orderId}`
*   **Description**: Retrieves the details of a specific order by its ID.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_ORDER_MANAGER`, `ROLE_SUPPORT`, or the `CUSTOMER` who owns the order.
*   **Path Variable**: `orderId` (Long) - The unique identifier of the order.
*   **Response**: `200 OK` with `OrderResponse` body.

#### `GET /api/orders/customer/{customerId}`
*   **Description**: Retrieves a paginated list of orders for a specific customer.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_ORDER_MANAGER`, `ROLE_SUPPORT`, or the `CUSTOMER` who owns the orders.
*   **Path Variable**: `customerId` (Long) - The unique identifier of the customer.
*   **Query Parameters**: `page` (int, default 0), `size` (int, default 20) for pagination.
*   **Response**: `200 OK` with `PagedResponse<OrderResponse>` body.

#### `PUT /api/orders/{orderId}/status`
*   **Description**: Updates the status of a specific order.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_ORDER_MANAGER`.
*   **Path Variable**: `orderId` (Long) - The unique identifier of the order.
*   **Request Body**: `UpdateOrderStatusRequest`
*   **Response**: `200 OK` with `OrderResponse` body.

#### `POST /api/orders/payment`
*   **Description**: Processes a payment for an order. This endpoint is typically called internally by the Order Service after an order is created, or by a customer to complete a pending payment.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Request Body**: `PaymentRequest` (from `payment-service`)
*   **Response**: `200 OK` with `PaymentResponse` body (from `payment-service`).

#### `PUT /api/orders/{orderId}/cancel`
*   **Description**: Cancels a specific order. This triggers compensating actions like releasing inventory and initiating refunds.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_ORDER_MANAGER`, or the `CUSTOMER` who owns the order.
*   **Path Variable**: `orderId` (Long) - The unique identifier of the order.
*   **Query Parameter**: `reason` (String) - The reason for cancellation.
*   **Response**: `200 OK` with `OrderResponse` body (representing the cancelled order).

#### `GET /api/orders`
*   **Description**: Retrieves a paginated list of all orders in the system. This is an administrative/support endpoint.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_ORDER_MANAGER`, `ROLE_SUPPORT`.
*   **Query Parameters**: `page` (int, default 0), `size` (int, default 20) for pagination.
*   **Response**: `200 OK` with `PagedResponse<OrderResponse>` body.

## 4. Data Transfer Objects (DTOs)

### `OrderResponse`
Represents the detailed information of an order.
*   `id`: Long - Unique identifier of the order.
*   `customerId`: Long - The ID of the customer who placed the order.
*   `customerName`: String - The name of the customer.
*   `customerEmail`: String - The email of the customer.
*   `status`: String - The current status of the order (e.g., PENDING, PAID, SHIPPED).
*   `totalAmount`: BigDecimal - The total monetary value of the order.
*   `shippingAddress`: String - The shipping address for the order.
*   `paymentId`: String - The ID of the payment transaction associated with this order.
*   `notes`: String - Any additional notes for the order.
*   `createdAt`: LocalDateTime - Timestamp of when the order was created.
*   `updatedAt`: LocalDateTime - Timestamp of the last update to the order.
*   `orderItems`: List of `OrderItemResponse` - A list of items included in the order.

### `OrderItemResponse`
Represents a single item within an order.
*   `id`: Long - Unique identifier of the order item.
*   `productId`: Long - The unique identifier of the product.
*   `productName`: String - The name of the product.
*   `quantity`: Integer - The quantity of the product in the order.
*   `unitPrice`: BigDecimal - The price per unit of the product at the time of order.
*   `subtotal`: BigDecimal - The total price for this order item (quantity * unitPrice).

### `CreateOrderRequest`
Request body for creating a new order.
*   `customerId`: Long (required) - The ID of the customer placing the order.
*   `shippingAddress`: String (required, max 500) - The shipping address for the order.
*   `orderItems`: List of `CreateOrderItemRequest` (required, not empty) - A list of items to include in the order.
*   `notes`: String (max 1000) - Any additional notes for the order.

### `CreateOrderItemRequest`
Request body for specifying an item to be included in a new order.
*   `productId`: Long (required) - The ID of the product.
*   `quantity`: Integer (required, min 1, max 1000) - The quantity of the product.

### `UpdateOrderStatusRequest`
Request body for updating the status of an order.
*   `status`: String (required, must be one of: PENDING, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED) - The new status for the order.
*   `notes`: String (max 500) - Any additional notes related to the status update.
