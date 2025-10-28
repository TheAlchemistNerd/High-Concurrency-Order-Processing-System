# High-Concurrency Order Processing System

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Architecture](#architecture)
  - [Architectural Model: Modular Monolith](#architectural-model-modular-monolith)
  - [Core Components and Service Boundaries](#core-components-and-service-boundaries)
  - [Database Design: Shared Database Model](#database-design-shared-database-model)
  - [Concurrency Model: Virtual Threads and CompletableFuture](#concurrency-model-virtual-threads-and-completablefuture)
  - [Consistency Model: Transactions and Saga Pattern](#consistency-model-transactions-and-saga-pattern)
- [API Reference](#api-reference)
  - [Authentication Service](/user-service/README.md#authentication-service-api-endpoints)
  - [User Profile Service](/user-service/README.md)
  - [Product Catalog Service](/product-service/README.md)
  - [Inventory Service](/inventory-service/README.md)
  - [Shopping Cart Service](/shopping-cart-service/README.md)
  - [Order Service](/order-service/README.md)
- [Configuration](#configuration)
- [Security: Authentication and Authorization](#security-authentication-and-authorization)
- [Tuning and Performance](#tuning-and-performance)
- [How to Build and Run](#how-to-build-and-run)
  - [Prerequisites](#prerequisites)
  - [Environment Setup](#environment-setup)
  - [Building and Running](#building-and-running)
- [Architectural Vision Documents (Concatenated)](#architectural-vision-documents-concatenated)
  - [Architectural Vision: Enhanced Order Management and Anonymous Cart Functionality](#architectural-vision-enhanced-order-management-and-anonymous-cart-functionality)
  - [Architectural Vision: Decoupling with a Product Catalog Service](#architectural-vision-decoupling-with-a-product-catalog-service)
  - [Architectural Vision: Implementing Role-Based Access Control (RBAC)](#architectural-vision-implementing-role-based-access-control-rbac)
  - [Architectural Vision: Advanced User Profile Management](#architectural-vision-advanced-user-profile-management)

## Project Overview

This project implements a robust and scalable e-commerce order processing system designed to handle a high volume of concurrent requests. Built with Spring Boot and leveraging modern Java concurrency features like Virtual Threads and `CompletableFuture`, it aims to provide a performant and resilient backend for an e-commerce platform.

The system is architected as a **modular monolith**, with clear service boundaries that lay the groundwork for a future decomposition into a full microservices architecture. It manages the entire e-commerce lifecycle, from user registration and product catalog management to shopping cart functionality, order orchestration, and payment processing.

## Features

*   **User Profile Management:**
    *   Local user registration with secure password hashing (BCrypt).
    *   OAuth2/Social login integration (Google, Facebook).
    *   Comprehensive user profile management, including multiple addresses.
    *   JWT-based authentication for securing endpoints.
*   **Product Catalog:**
    *   Full CRUD operations for managing products.
    *   Publicly accessible endpoints for browsing and searching products.
*   **Inventory Management:**
    *   Real-time stock checking.
    *   Transactional inventory reservation, release, and commit operations to prevent overselling.
*   **Shopping Cart:**
    *   Persistent shopping carts for authenticated users.
    *   Functionality to add, update, and remove items.
*   **Order Management:**
    *   Orchestration of the order creation process, including inventory reservation.
    *   Paginated order history for customers.
    *   Order status management (`PENDING`, `PAID`, `SHIPPED`, etc.).
    *   Order cancellation with compensating actions for inventory and payments.
*   **Payment Processing:**
    *   Abstracted payment gateway integration, allowing for multiple providers (e.g., Mock, Stripe).
    *   Payment processing and refund capabilities.
*   **High Concurrency:**
    *   Utilizes Java Virtual Threads to efficiently handle a large number of I/O-bound operations, maximizing throughput.
    *   Asynchronous and non-blocking execution flows using `CompletableFuture`.
*   **Role-Based Access Control (RBAC):**
    *   Granular access control for all API endpoints based on user roles (`CUSTOMER`, `ADMIN`, `PRODUCT_MANAGER`, etc.).

## Architecture

### Architectural Model: Modular Monolith

The application is designed as a **modular monolith**. While it runs as a single deployable unit, the codebase is organized into distinct, domain-oriented modules (`user-service`, `product-service`, `order-service`, etc.). This approach provides several advantages:

*   **Clear Boundaries:** Each module has a well-defined responsibility and a public API (exposed via its controllers), mimicking the structure of microservices.
*   **Simplified Development:** It avoids the complexities of distributed systems (e.g., network latency, service discovery) during the initial development phases.
*   **Path to Microservices:** The clear separation of concerns makes it significantly easier to extract these modules into independently deployable microservices in the future.

### Core Components and Service Boundaries

The project is divided into the following logical services, each with its own package structure containing controllers, services, repositories, and domain entities:

*   **`user-service`**: Manages all aspects of user identity, including registration, authentication (local and OAuth2), profile data, and address management. It is the source of truth for user roles.
*   **`product-service`**: Manages the product catalog. It is responsible for all descriptive information about products, such as name, description, and price.
*   **`inventory-service`**: Manages the stock levels of products. This service is highly transactional, handling reservations, releases, and commits of inventory.
*   **`shopping-cart-service`**: Manages the state of users' shopping carts before they proceed to checkout.
*   **`order-service`**: Acts as the central orchestrator for the order lifecycle. It communicates with all other services to create, process, and manage orders.
*   **`payment-service`**: Abstracted service for handling all interactions with payment gateways. It is designed to support multiple payment providers.
*   **`common`**: A shared module containing cross-cutting concerns like custom exceptions, DTOs, and the asynchronous configuration.

### Database Design: Shared Database Model

A key architectural decision in this modular monolith is the use of a **single, shared PostgreSQL database** (`ecommerce_orders`). All services share this database, which has significant implications for consistency and data management.

*   **Advantages:**
    *   **Simplified Transactions:** It allows for the use of standard ACID transactions across different logical services. For example, creating an order and reserving inventory can be wrapped in a single `@Transactional` block, ensuring atomicity.
    *   **Easier Data Management:** Avoids the need for complex data synchronization mechanisms between services.
*   **Disadvantages:**
    *   **High Coupling:** The shared database is a major point of coupling between the services. A change to the database schema can potentially impact multiple modules.
    *   **Scalability:** The database can become a central bottleneck, limiting the scalability of the entire system.

### Concurrency Model: Virtual Threads and CompletableFuture

The application is designed to handle high concurrency by leveraging modern Java features:

*   **Virtual Threads:** The `AsyncConfig` class configures a `ThreadPoolExecutor` that uses a `VirtualThreadFactory`. This means that all asynchronous operations submitted to the `virtualThreadExecutor` are executed on lightweight, JVM-managed virtual threads. This is ideal for I/O-bound applications like this one, as it allows the system to handle a very large number of concurrent requests without being limited by the number of platform threads.
*   **`CompletableFuture`:** The service layer makes extensive use of `CompletableFuture` to create non-blocking, asynchronous execution flows. This is particularly important in the `OrderServiceImpl`, where multiple downstream services are called during the order creation process.

However, it is crucial to note that the current implementation often uses `.join()` to block and wait for the result of these asynchronous operations. This makes the logic within a single request **synchronous and blocking**, even though it runs on a virtual thread. While this simplifies the code, it does not take full advantage of the non-blocking capabilities of `CompletableFuture`.

### Consistency Model: Transactions and Saga Pattern

Consistency across the different modules is a major challenge. The application uses a hybrid approach:

1.  **ACID Transactions:** Because all services share a single database, ACID transactions are the primary mechanism for ensuring consistency for operations within a single service or across services that touch the same database. The `createOrder` method in `OrderServiceImpl` is annotated with `@Transactional`, which wraps the entire order creation process (including inventory reservation) in a single, atomic transaction. If any part of the process fails, the entire transaction is rolled back.

2.  **Saga Pattern (Incomplete):** For operations that span transactional boundaries (especially calls to external services like a payment gateway), the application attempts to use the Saga pattern. A saga is a sequence of local transactions where each transaction updates the database and publishes a message or event to trigger the next transaction.
    *   **Compensating Actions:** The `cancelOrder` method in `OrderServiceImpl` implements compensating actions. If a `PAID` order is canceled, it calls `paymentService.refundPayment()` and `inventoryService.releaseInventory()`. This is a crucial part of the saga pattern, ensuring that the system can be returned to a consistent state after a failure.
    *   **Incompleteness:** The current implementation is not a "full" saga because it relies on synchronous, direct service calls rather than asynchronous, event-driven communication via a message broker. This makes it less resilient than a true saga implementation.

## API Reference

The following is a summary of the main API endpoints exposed by the application.

### Authentication Service (`/api/auth`)

| Method | Endpoint                  | Description                      | Roles Permitted |
|--------|---------------------------|----------------------------------|-----------------|
| `POST` | `/api/auth/register`      | Registers a new customer.        | `Anonymous`     |
| `POST` | `/api/auth/login`         | Authenticates a user and returns a JWT. | `Anonymous`     |
| `GET`  | `/api/auth/login/oauth2`  | Initiates OAuth2 login flow.     | `Anonymous`     |

### User Profile Service (`/api/users`)

| Method   | Endpoint                       | Description                               | Roles Permitted        |
|----------|--------------------------------|-------------------------------------------|------------------------|
| `GET`    | `/api/users/me`                | Gets the profile of the logged-in user.   | `CUSTOMER`             |
| `PUT`    | `/api/users/me`                | Updates the profile of the logged-in user.| `CUSTOMER`             |
| `GET`    | `/api/users/me/addresses`      | Gets all addresses for the logged-in user.| `CUSTOMER`             |
| `POST`   | `/api/users/me/addresses`      | Adds a new address for the logged-in user.| `CUSTOMER`             |
| `PUT`    | `/api/users/me/addresses/{id}` | Updates an address for the logged-in user.| `CUSTOMER`             |
| `DELETE` | `/api/users/me/addresses/{id}` | Deletes an address for the logged-in user.| `CUSTOMER`             |
| `POST`   | `/api/users/me/change-password`| Changes the password for the logged-in user.| `CUSTOMER`             |
| `DELETE` | `/api/users/me`                | Deletes the account of the logged-in user.| `CUSTOMER`             |
| `GET`    | `/api/users/{userId}`          | Gets the profile of any user by ID.       | `ADMIN`, `SUPPORT`     |
| `GET`    | `/api/users/`                  | Gets a list of all users.                 | `ADMIN`                |

### Product Catalog Service (`/api/products`)

| Method   | Endpoint                | Description                   | Roles Permitted            |
|----------|-------------------------|-------------------------------|----------------------------|
| `GET`    | `/api/products`         | Gets a list of all products.  | `Anonymous`                |
| `GET`    | `/api/products/{id}`    | Gets a single product by ID.  | `Anonymous`                |
| `POST`   | `/api/products`         | Creates a new product.        | `ADMIN`, `PRODUCT_MANAGER` |
| `PUT`    | `/api/products/{id}`    | Updates an existing product.  | `ADMIN`, `PRODUCT_MANAGER` |
| `DELETE` | `/api/products/{id}`    | Deletes a product.            | `ADMIN`, `PRODUCT_MANAGER` |

### Inventory Service (`/api/inventory`)

| Method | Endpoint                               | Description                               | Roles Permitted            |
|--------|----------------------------------------|-------------------------------------------|----------------------------|
| `GET`  | `/api/inventory/products/{id}/check`   | Checks if a product has enough stock.     | `Anonymous`                |
| `GET`  | `/api/inventory/products/{id}`         | Gets the inventory level for a product.   | `Anonymous`                |
| `POST` | `/api/inventory/products/{id}/reserve` | Reserves a quantity of a product.         | `ORDER_MANAGER` (Internal) |
| `POST` | `/api/inventory/products/{id}/release` | Releases a reservation for a product.     | `ORDER_MANAGER` (Internal) |
| `POST` | `/api/inventory/products/{id}/commit`  | Commits a stock deduction after a sale.   | `ORDER_MANAGER` (Internal) |
| `POST` | `/api/inventory/products/{id}/restock` | Adds new stock for a product.             | `ADMIN`, `PRODUCT_MANAGER` |

### Shopping Cart Service (`/api/cart`)

| Method   | Endpoint                               | Description                         | Roles Permitted |
|----------|----------------------------------------|-------------------------------------|-----------------|
| `GET`    | `/api/cart/{customerId}`               | Gets the user's shopping cart.      | `CUSTOMER`      |
| `POST`   | `/api/cart/{customerId}/items`         | Adds an item to the cart.           | `CUSTOMER`      |
| `PUT`    | `/api/cart/{customerId}/items/{prodId}`| Updates the quantity of an item.    | `CUSTOMER`      |
| `DELETE` | `/api/cart/{customerId}/items/{prodId}`| Removes an item from the cart.      | `CUSTOMER`      |
| `DELETE` | `/api/cart/{customerId}`               | Clears the entire shopping cart.    | `CUSTOMER`      |

### Order Service (`/api/orders`)

| Method   | Endpoint                       | Description                               | Roles Permitted                      |
|----------|--------------------------------|-------------------------------------------|--------------------------------------|
| `POST`   | `/api/orders`                  | Creates a new order.                      | `CUSTOMER`                           |
| `GET`    | `/api/orders/{orderId}`        | Gets an order by ID.                      | `ADMIN`, `ORDER_MANAGER`, `SUPPORT`, or owner |
| `GET`    | `/api/orders/customer/{custId}`| Gets all orders for a customer.           | `ADMIN`, `ORDER_MANAGER`, `SUPPORT`, or owner |
| `PUT`    | `/api/orders/{orderId}/status` | Updates the status of an order.           | `ADMIN`, `ORDER_MANAGER`             |
| `POST`   | `/api/orders/payment`          | Processes a payment for an order.         | `CUSTOMER`                           |
| `PUT`    | `/api/orders/{orderId}/cancel` | Cancels an order.                         | `ADMIN`, `ORDER_MANAGER`, or owner   |
| `GET`    | `/api/orders`                  | Gets a list of all orders.                | `ADMIN`, `ORDER_MANAGER`, `SUPPORT`  |



## Configuration

The main configuration for the application is located in `app/src/main/resources/application.yml`.

*   **`server`**: Configures the embedded Tomcat server, including the port and thread pool settings.
*   **`spring.datasource`**: Configures the connection to the PostgreSQL database, including the URL, username, password, and Hikari connection pool settings.
*   **`spring.jpa`**: Configures JPA and Hibernate, including the DDL auto-generation strategy and database dialect.
*   **`spring.security`**: Configures security settings, including the JWT secret key and expiration time, and OAuth2 client credentials.
*   **`app`**: Contains custom application properties, such as the payment provider to use, timeouts for external services, and the configuration for the virtual thread pool.
*   **`springdoc`**: Configures the OpenAPI documentation and Swagger UI.

## Security: Authentication and Authorization

The application uses a robust security model based on Spring Security, JWT, and Role-Based Access Control (RBAC).

*   **Authentication:**
    *   **Local:** Users can register and log in with an email and password. Passwords are securely hashed using BCrypt.
    *   **OAuth2:** Users can authenticate using Google or Facebook. The system will automatically create a new user account if one doesn't exist for the given email.
*   **JWT:** Upon successful authentication, a JSON Web Token (JWT) is generated and returned to the client. This token contains the user's email and roles and is used to authenticate subsequent requests.
*   **Authorization:**
    *   **`@PreAuthorize`:** API endpoints are secured using `@PreAuthorize` annotations, which allow for expressive, fine-grained access control rules based on user roles.
    *   **Roles:** The system uses a set of predefined roles (`CUSTOMER`, `ADMIN`, `PRODUCT_MANAGER`, `ORDER_MANAGER`, `SUPPORT`) to enforce the principle of least privilege.

## Tuning and Performance

For a high-concurrency application, tuning is critical. The following parameters in `application.yml` are the most important for performance:

*   **`server.tomcat.threads.max`**: The maximum number of threads in the Tomcat thread pool. This determines the maximum number of concurrent HTTP requests the application can handle.
*   **`spring.datasource.hikari.maximum-pool-size`**: The maximum number of connections in the database connection pool. This is a critical parameter, as the database is often the primary bottleneck.
*   **`app.virtual-threads.executor.max-pool-size`**: The maximum number of virtual threads that can be created by the custom `ThreadPoolExecutor`. This can be used to limit the overall concurrency of asynchronous tasks.
*   **Timeouts:** The `app.payment.gateway.timeout` and `app.inventory.service.timeout` properties are important for resilience, preventing slow downstream services from causing cascading failures.

## How to Build and Run

### Prerequisites

*   Java Development Kit (JDK) 22 or higher
*   Gradle 8.x or higher
*   PostgreSQL 14 or higher
*   Git

### Environment Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/TheAlchemistNerd/high-concurrency-order-processing.git
    cd high-concurrency-order-processing
    ```

2.  **Configure the database:**
    *   Create a PostgreSQL database (e.g., `ecommerce_orders`).
    *   Update the `spring.datasource` properties in `app/src/main/resources/application.yml` with your database URL, username, and password.

### Building and Running

1.  **Build the project:**
    ```bash
    ./gradlew clean build
    ```

2.  **Run the application:**
    ```bash
    java -jar app/build/libs/high-concurrency-order-processing-1.0.0.jar
    ```

The application will be available at `http://localhost:8080`. The Swagger UI for API documentation can be accessed at `http://localhost:8080/swagger-ui.html`.

---

## Architectural Vision Documents (Concatenated)

### [Architectural Vision: Enhanced Order Management and Anonymous Cart Functionality](/ORDER_AND_CART_ARCHITECTURE.md)

### [Architectural Vision: Decoupling with a Product Catalog Service](/PRODUCT_CATALOG_SERVICE_ARCHITECTURE.md)

### [Architectural Vision: Implementing Role-Based Access Control (RBAC)](/RBAC_ARCHITECTURE.md)

### [Architectural Vision: Advanced User Profile Management](/USER_PROFILE_MANAGEMENT_ARCHITECTURE.md)

### [Architectural Vision: Payment Service](/payment-service/README.md)

### [User Service Documentation](/user-service/README.md)

### [Product Service Documentation](/product-service/README.md)

### [Inventory Service Documentation](/inventory-service/README.md)

### [Shopping Cart Service Documentation](/shopping-cart-service/README.md)

### [Order Service Documentation](/order-service/README.md)

### [Design Decisions: Edge Cases and Administrative Actions](/DESIGN_DECISIONS.md)