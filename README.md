# High-Concurrency Order Processing System

## Project Overview

This project implements a robust and scalable e-commerce order processing system designed to handle a high volume of concurrent requests. Built with Spring Boot and leveraging modern Java concurrency features like Virtual Threads and `CompletableFuture`, it aims to provide a performant and resilient backend for an e-commerce platform. The system is structured with a focus on clear service boundaries, laying the groundwork for a future microservices architecture.

## Features

*   **Order Management:**
    *   Create new orders with multiple items.
    *   Retrieve order details by ID.
    *   View customer-specific order history with pagination.
    *   Update order statuses (e.g., PENDING, PAID, SHIPPED, CANCELLED).
    *   Process payments for pending orders.
    *   Cancel orders with automatic inventory restoration.
    *   Retrieve all orders with pagination (admin functionality).
*   **Inventory Management:**
    *   Check product stock availability.
    *   Reserve and restore product inventory during order processing and cancellation.
*   **Payment Processing:**
    *   Simulated integration with an external payment gateway for processing payments and refunds.
*   **Shopping Cart Functionality:**
    *   Retrieve a customer's shopping cart.
    *   Add items to the cart.
    *   Update quantities of items in the cart.
    *   Remove items from the cart.
    *   Clear the entire shopping cart.
*   **User Authentication:**
    *   Customer registration with email and password.
    *   JWT-based authentication for securing endpoints.
*   **High Concurrency:**
    *   Utilizes Java Virtual Threads for efficient handling of I/O-bound operations, minimizing thread blocking and maximizing throughput.
    *   Asynchronous programming with `CompletableFuture` for non-blocking execution flows.

## Architecture

The system is designed with a layered architecture, emphasizing separation of concerns and preparing for a transition to a microservices-based deployment.

### Core Components

*   **Controller Layer:** Handles incoming HTTP requests, delegates to the service layer, and returns appropriate responses.
*   **Service Layer:** Contains the core business logic. This layer is designed to be highly concurrent, utilizing `CompletableFuture` and Virtual Threads for I/O-bound operations.
*   **Repository Layer:** Abstracts data access operations, interacting with the PostgreSQL database using Spring Data JPA.
*   **Domain Layer:** Defines the core business entities and their relationships.

### Service Boundaries and API Design

A key architectural principle is the clear separation of services, even within the current monolithic structure. Services like `InventoryService` and `PaymentService` have their own REST controllers (`InventoryController`, `PaymentController`) for two main reasons:

1.  **Microservice-Ready:** This design defines the public API for each service, making it straightforward to decompose the application into independent microservices in the future. Each controller represents the boundary of a future service.
2.  **Independent Functionality:** It allows other systems (e.g., an admin UI, a warehouse management system) to interact with services directly without going through the `OrderService`. For example, an admin might need to check stock levels or issue a refund independently of an order.

This approach makes the system more flexible, scalable, and easier to test and maintain.

### Microservices Boundaries (Proposed)

The current monolithic structure is built with clear domain boundaries, making it suitable for future decomposition into independent microservices:

*   **`customer-service`**: Responsible for managing customer data, authentication, and user profiles.
*   **`product-service`**: Manages product information, including details, pricing, and availability.
*   **`inventory-service`**: Dedicated to tracking and managing product stock levels, handling reservations and restorations.
*   **`order-service`**: Orchestrates the entire order lifecycle, from creation to fulfillment, interacting with other services as needed.
*   **`payment-service`**: Handles all payment-related transactions, integrating with external payment gateways.
*   **`shopping-cart-service`**: Manages the temporary state of a user's shopping cart before checkout.

### Asynchronous Programming with Virtual Threads

A key architectural decision for achieving high concurrency is the extensive use of Java Virtual Threads (Project Loom) combined with `CompletableFuture`.

*   **Virtual Threads**: Lightweight, user-mode threads managed by the JVM, ideal for I/O-bound tasks. They significantly reduce the overhead associated with traditional platform threads, allowing the application to handle a much larger number of concurrent connections without resource exhaustion.
*   **`CompletableFuture`**: Provides a powerful API for asynchronous programming, enabling non-blocking execution flows. It allows for chaining dependent asynchronous operations, handling errors, and composing complex concurrent logic.

This combination is applied in services performing I/O operations, such as:

*   **`InventoryService`**: Database interactions for stock checks, reservations, and restorations.
*   **`PaymentService` & `PaymentGatewayClient`**: Simulated external API calls to a payment gateway.
*   **`OrderService`**: Complex workflows involving multiple database operations and interactions with `InventoryService` and `PaymentService`.
*   **`AuthenticationService`**: Database lookups for user authentication.
*   **`CustomUserDetailsService`**: Database lookups for loading user details during security context creation.
*   **`ShoppingCartService`**: Database interactions for managing cart contents.

### Design Patterns

The project adheres to several common design patterns to ensure maintainability, scalability, and testability:

*   **Service Layer**: Encapsulates business logic, separating it from controllers and data access.
*   **Repository Pattern**: Abstracts data access operations, providing a clean interface for CRUD operations.
*   **Facade Pattern**: The `OrderService` acts as a facade, simplifying complex workflows by coordinating multiple underlying services and repositories.
*   **Dependency Injection**: Used extensively via Spring's IoC container for loose coupling and testability.
*   **Functional Programming**: Leverages Java Stream API and lambda expressions for concise and expressive data manipulation.

## Technologies Used

*   **Java 22**: The core programming language, utilizing Project Loom (Virtual Threads).
*   **Spring Boot 3.x**: Framework for building robust, production-ready Spring applications.
*   **Spring Data JPA**: For simplified data access and ORM with Hibernate.
*   **PostgreSQL**: Relational database for persistent storage.
*   **Gradle**: Build automation tool.
*   **Lombok**: Reduces boilerplate code (getters, setters, constructors).
*   **Spring Security**: For authentication and authorization (basic setup).
*   **Micrometer & Prometheus**: For application metrics and monitoring.
*   **Springdoc OpenAPI UI**: For API documentation (Swagger UI).
*   **Testcontainers**: For integration testing with real services (e.g., PostgreSQL).

## Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

*   **Java Development Kit (JDK) 22 or higher**: This project specifically uses Java 22 for Virtual Threads.
*   **Gradle**: Version 8.x or higher.
*   **PostgreSQL**: Version 14 or higher.
*   **Git**: For cloning the repository.

### Environment Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/TheAlchemistNerd/high-concurrency-order-processing.git
    cd high-concurrency-order-processing
    ```

2.  **Set `JAVA_HOME`:**
    Ensure your `JAVA_HOME` environment variable points to your JDK 22 installation directory.
    *   **Windows:**
        ```bash
        set JAVA_HOME="C:\Program Files\Java\jdk-22"
        set PATH=%JAVA_HOME%\bin;%PATH%
        ```
    *   **macOS/Linux:**
        ```bash
        export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-22.jdk/Contents/Home
        export PATH=$JAVA_HOME/bin:$PATH
        ```
    (Adjust paths according to your actual JDK installation.)

3.  **PostgreSQL Database Setup:**
    *   Create a new PostgreSQL database for the application (e.g., `ecommerce_db`).
    *   Create a user with appropriate permissions (e.g., `ecommerce_user` with password `password`).
    ```sql
    CREATE DATABASE ecommerce_db;
    CREATE USER ecommerce_user WITH PASSWORD 'password';
    GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;
    ```

4.  **Application Configuration:**
    *   Open `src/main/resources/application.properties`.
    *   Update the database connection details to match your PostgreSQL setup:
        ```properties
        spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_db
        spring.datasource.username=ecommerce_user
        spring.datasource.password=password
        spring.jpa.hibernate.ddl-auto=update # or create-drop for development
        spring.jpa.show-sql=true
        spring.jpa.properties.hibernate.format_sql=true
        ```
    *   (Optional) Configure JWT secret and payment gateway details if needed.

## Building and Running the Application

1.  **Build the project:**
    ```bash
    ./gradlew clean build
    ```
    On Windows, use `gradlew.bat clean build`.

2.  **Run the application:**
    ```bash
    java -jar build/libs/high-concurrency-order-processing-1.0.0.jar
    ```
    Alternatively, you can run it directly via Gradle:
    ```bash
    ./gradlew bootRun
    ```

The application will start on `http://localhost:8080`.

## API Endpoints

Once the application is running, you can access the API documentation via Swagger UI:
`http://localhost:8080/swagger-ui.html`

Here's a summary of the main API endpoints:

*   **Authentication:**
    *   `POST /api/auth/register`: Customer registration.
    *   `POST /api/auth/login`: User login.
*   **Orders:**
    *   `POST /api/orders`: Create a new order.
    *   `GET /api/orders/{orderId}`: Get order details by ID.
    *   `GET /api/orders/customer/{customerId}`: Get orders for a specific customer.
    *   `PUT /api/orders/{orderId}/status`: Update order status.
    *   `POST /api/orders/payment`: Process payment for an order.
    *   `PUT /api/orders/{orderId}/cancel`: Cancel an order.
    *   `GET /api/orders`: Get all orders (admin).
*   **Inventory:**
    *   `GET /api/inventory/products/{productId}`: Check product inventory.
    *   `POST /api/inventory/products/{productId}/reserve`: Reserve inventory.
    *   `POST /api/inventory/products/{productId}/restore`: Restore inventory.
*   **Payments:**
    *   (Handled internally by OrderService, or directly via PaymentGatewayClient if exposed)
*   **Shopping Cart:**
    *   `GET /api/cart/{customerId}`: Get shopping cart contents.
    *   `POST /api/cart/{customerId}/items`: Add item to cart.
    *   `PUT /api/cart/{customerId}/items/{productId}`: Update item quantity in cart.
    *   `DELETE /api/cart/{customerId}/items/{productId}`: Remove item from cart.
    *   `DELETE /api/cart/{customerId}`: Clear shopping cart.

## Future Enhancements

*   **Full Microservices Deployment**: Decompose the monolith into independent services.
*   **Message Queues**: Implement asynchronous communication between services using Kafka or RabbitMQ for event-driven architecture.
*   **Circuit Breakers & Retries**: Implement resilience patterns for external service calls.
*   **Distributed Tracing**: Integrate with tools like Zipkin or Jaeger for better observability.
*   **Comprehensive Testing**: Expand unit, integration, and end-to-end tests.
*   **User Profile Management**: Allow users to view and update their profile information.
*   **Role-Based Access Control (RBAC)**: Enhance the authorization logic with more granular roles and permissions.
*   **Product Catalog Management**: Dedicated service for managing product data.
*   **Real Payment Gateway Integration**: Replace the simulated payment gateway with a real one (e.g., Stripe, PayPal).

## Contributing

Contributions are welcome! Please fork the repository and submit pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
