# Architectural Vision: Implementing Role-Based Access Control (RBAC)

## Introduction

As the e-commerce platform evolves from a monolith into a suite of specialized microservices—such as the proposed `Product Catalog Service` and `User Profile Service`—the need for a granular and robust security model becomes paramount. A simple authenticated-vs-unauthenticated paradigm is no longer sufficient. This document outlines a vision for implementing a comprehensive Role-Based Access Control (RBAC) system. This system will ensure that users and system operators only have access to the data and operations necessary for their designated functions, adhering to the principle of least privilege.

## 1. Defining User Roles

The foundation of an RBAC system is a well-defined set of roles that map to the distinct functions within the e-commerce domain. The following roles provide a comprehensive starting point:

*   `ROLE_CUSTOMER`: This is the baseline role for all registered users. Customers can manage their own profiles, view their order history, manage their shopping cart, and place new orders.

*   `ROLE_ADMIN`: A superuser role with wide-ranging permissions. Admins can manage users, override system settings, and access all data across all services. This role should be assigned sparingly.

*   `ROLE_PRODUCT_MANAGER`: A specialized business role responsible for managing the product catalog. This user can create, update, and delete products and their descriptive information. They may also have permissions to update inventory levels.

*   `ROLE_ORDER_MANAGER`: A role for operational staff responsible for fulfillment. They can view all customer orders, update order statuses (e.g., from `PAID` to `SHIPPED`), process cancellations, and handle returns.

*   `ROLE_SUPPORT`: A role designed for customer service representatives. They would have read-only access to customer profiles and their associated orders to assist with inquiries and troubleshooting. They should not be able to modify data.

## 2. Proposed RBAC Architecture

To implement this system, we will enhance the existing security infrastructure, leveraging the power of Spring Security and JWTs.

### 2.1. Data Model

The `UserService` would be the owner of role information:

1.  **`Role` Entity**: A new database table/entity to store the available roles (e.g., `id`, `name`).
2.  **User-Role Relationship**: A many-to-many relationship between the `User` and `Role` entities. This allows a single user to have multiple roles (e.g., a user who is both an `ADMIN` and an `ORDER_MANAGER`).

### 2.2. JWT Enhancement

For efficient, stateless authorization across microservices, user roles must be embedded directly into the JSON Web Token (JWT).

1.  **Login Flow**: When a user authenticates, the `AuthenticationService` will query the `UserService` to retrieve their assigned roles.
2.  **Token Creation**: The `JwtTokenProvider` will then add a `roles` claim to the JWT payload (e.g., `"roles": ["ROLE_CUSTOMER", "ROLE_ORDER_MANAGER"]`).
3.  **Resource Server Validation**: When a microservice receives a request, it will validate the JWT. The Spring Security configuration on the resource server will then use the roles within the token to make authorization decisions without needing to call the `UserService` again, making the process highly efficient.

### 2.3. Enforcement with Spring Security

Endpoint protection will be enforced using Spring Security's method security annotations, which allow for expressive, fine-grained control.

*   `@PreAuthorize("hasRole('ADMIN')")`: Only allows users with the `ADMIN` role.
*   `@PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER')")`: Allows users with either role.
*   `@PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")`: A powerful expression that ensures a user is a `CUSTOMER` AND is only accessing their own data.

## 3. Securing API Endpoints: A Comprehensive Map

The following is a detailed breakdown of how RBAC would be applied across all existing and proposed API endpoints.

--- 

### Authentication Service (`/api/auth`)
*   `POST /register`: `Anonymous` (Permit All)
*   `POST /login`: `Anonymous` (Permit All)

### User Profile Service (`/api/users`)
*   `GET /me`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `PUT /me`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `GET /me/addresses`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `POST /me/addresses`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `PUT /me/addresses/{addressId}`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `DELETE /me/addresses/{addressId}`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `POST /me/change-password`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `DELETE /me`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `GET /{userId}`: `@PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")`
*   `GET /`: `@PreAuthorize("hasRole('ADMIN')")`

### Shopping Cart Service (`/api/cart`)
*   `GET /{customerId}`: `@PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")`
*   `POST /{customerId}/items`: `@PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")`
*   `PUT /{customerId}/items/{productId}`: `@PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")`
*   `DELETE /{customerId}/items/{productId}`: `@PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")`
*   `DELETE /{customerId}`: `@PreAuthorize("hasRole('CUSTOMER') and #customerId == authentication.principal.id")`

### Product Catalog Service (`/api/products`)
*   `GET /`: `Anonymous` (Permit All)
*   `GET /{productId}`: `Anonymous` (Permit All)
*   `POST /`: `@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")`
*   `PUT /{productId}`: `@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")`
*   `DELETE /{productId}`: `@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")`

### Inventory Service (`/api/inventory`)
*   `GET /products/{productId}`: `Anonymous` (Permit All)
*   `POST /products/{productId}/reserve`: `@PreAuthorize("hasRole('ORDER_MANAGER')")` - This is an internal, service-to-service call initiated by `OrderService`.
*   `POST /products/{productId}/restore`: `@PreAuthorize("hasRole('ORDER_MANAGER')")` - Also an internal, service-to-service call.
*   `POST /products/{productId}/restock`: `@PreAuthorize("hasAnyRole('ADMIN', 'PRODUCT_MANAGER')")` - For manually adding new stock.

### Order Service (`/api/orders`)
*   `POST /`: `@PreAuthorize("hasRole('CUSTOMER')")`
*   `GET /{orderId}`: `@PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT') or (@orderRepository.findById(#orderId).orElse(null)?.customer.id == authentication.principal.id)")` - Complex rule: allow staff OR the customer who owns the order.
*   `GET /customer/{customerId}`: `@PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT') or (#customerId == authentication.principal.id)")`
*   `PUT /{orderId}/status`: `@PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER')")`
*   `POST /payment`: `@PreAuthorize("hasRole('CUSTOMER')")` - Should verify the order belongs to the user.
*   `PUT /{orderId}/cancel`: `@PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER') or (@orderRepository.findById(#orderId).orElse(null)?.customer.id == authentication.principal.id)")`
*   `GET /`: `@PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER', 'SUPPORT')")`

--- 

## Conclusion

By systematically defining roles and applying them to each API endpoint, we can build a highly secure and well-structured system. This RBAC architecture provides the necessary controls to protect sensitive data and operations while enabling different user types to perform their functions efficiently. It is a critical step in maturing the platform from a simple application into a professional, enterprise-grade e-commerce system, providing security, operational efficiency, and a clear path for auditing and compliance.
