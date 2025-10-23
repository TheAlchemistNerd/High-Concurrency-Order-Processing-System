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

### 2.4. Authentication and Authorization Details

The `user-service` module is central to managing user identities and access control. It provides multiple authentication paths, all converging on a robust, JWT-based authorization mechanism.

#### 2.4.1. Authentication Paths

1.  **Local Username/Password Authentication:**
    *   **Flow:** Users provide their email and password to the `AuthController.login()` endpoint. The `AuthenticationService.authenticate()` method then verifies these credentials against the stored user data and securely hashed passwords.
    *   **JWT Issuance:** Upon successful authentication, the `JwtTokenProvider.generateToken()` method is invoked to create a JSON Web Token (JWT). This JWT encapsulates essential user information, including their assigned roles.
    *   **Client Response:** The client receives a `LoginResponse` containing the newly generated JWT, which is then used for subsequent authenticated requests.

2.  **OAuth2/Social Authentication (e.g., Google, Facebook):**
    *   **Flow Initiation:** Users initiate login by clicking a "Login with Google" or "Login with Facebook" button, which directs them to `AuthController.oauth2Login()`. This, in turn, redirects the user to the respective OAuth2 provider for authentication.
    *   **Provider Authentication & Callback:** After successful authentication with the OAuth2 provider, the user is redirected back to the application with an authorization code. Spring Security intercepts this callback.
    *   **User Loading/Creation:** The `SecurityConfig` utilizes the `CustomOAuth2UserService` to process the user information received from the OAuth2 provider. This service either loads an existing local user account associated with the provided email or creates a new user account if one doesn't exist, assigning default roles (e.g., `ROLE_CUSTOMER`).
    *   **JWT Issuance:** Following successful user loading/creation, the `OAuth2AuthenticationSuccessHandler` is invoked. This handler is responsible for generating a JWT (typically by calling `JwtTokenProvider.generateToken()`) and returning it to the client, often via a redirect with the token embedded in a URL parameter or a cookie.
    *   **Client Usage:** The client then uses this JWT for all subsequent authenticated requests.

#### 2.4.2. Authorization Mechanism

Once a user is authenticated (via either local credentials or OAuth2) and possesses a JWT, authorization for subsequent requests is handled as follows:

*   **JWT Reception:** The client includes the JWT in the `Authorization` header of every request to protected resources.
*   **Filter Interception:** The `JwtAuthenticationFilter` (configured in `SecurityConfig.java`) intercepts incoming requests.
*   **Token Validation:** The filter extracts the JWT and uses `JwtTokenProvider.validateToken()` to verify its signature, expiration, and integrity.
*   **Role Extraction:** Upon successful validation, `JwtTokenProvider.getRoleFromToken()` extracts the user's role(s) from the JWT's claims.
*   **Security Context:** A Spring Security `Authentication` object, populated with the user's identity and extracted roles, is created and set in the `SecurityContextHolder`.
*   **Access Control:** `@PreAuthorize` annotations, strategically placed on controller methods (e.g., in `UserController`), then leverage the information in the `SecurityContextHolder` to make fine-grained access control decisions, ensuring that only users with the necessary roles can perform specific actions.

#### 2.4.3. Soundness Assessment

*   **Strengths:**
    *   **Stateless JWTs:** The system effectively utilizes JWTs for stateless authentication and authorization, which is crucial for scalability and resilience in a microservices environment.
    *   **Clear Role-Based Authorization:** A well-defined set of roles (`ROLE_CUSTOMER`, `ROLE_ADMIN`, etc.) is used to enforce access policies, aligning with the principle of least privilege.
    *   **Seamless OAuth2 Integration:** The integration with OAuth2 providers (Google, Facebook) provides a convenient and secure way for users to authenticate.
    *   **Secure Password Hashing:** Passwords for local accounts are securely stored using `BCryptPasswordEncoder`.
    *   **Fine-Grained `@PreAuthorize` Rules:** The use of `@PreAuthorize` annotations allows for expressive and precise control over API endpoint access.
    *   **Good Separation of Concerns:** Responsibilities are clearly divided among `AuthenticationService` (core login/registration), `JwtTokenProvider` (JWT specifics), and `CustomOAuth2UserService` (OAuth2 user loading).

*   **Potential Future Improvements:**
    *   **`OAuth2AuthenticationSuccessHandler` Implementation Review:** A detailed review of the `OAuth2AuthenticationSuccessHandler` implementation is crucial to ensure it correctly generates and returns the JWT after OAuth2 login, passing the appropriate roles obtained from the `AppUserDetails`.
    *   **Multiple Roles in JWT Claim:** The `JwtTokenProvider.generateToken()` method currently accepts a single `String role` parameter, which is then stored as a comma-separated string in the JWT's "role" claim. While `AppUserDetails` correctly parses this into multiple `GrantedAuthority` objects, it is generally considered better practice to store multiple roles as a list or array directly within the JWT claim. This would enhance clarity and potentially simplify parsing in other contexts.
    *   **Dynamic Role Assignment for OAuth2 Users:** When a new user registers via OAuth2, they are currently assigned a default `ROLE_CUSTOMER`. Implementing a mechanism for administrators to dynamically assign additional roles (e.g., `ROLE_PRODUCT_MANAGER`, `ROLE_ORDER_MANAGER`) to these users post-registration would provide greater flexibility and administrative control.

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
*   `GET /products/{productId}/check`: `Anonymous` (Permit All)
*   `POST /products/{productId}/reserve`: `@PreAuthorize("hasRole('ORDER_MANAGER')")` - This is an internal, service-to-service call initiated by `OrderService`.
*   `POST /products/{productId}/release`: `@PreAuthorize("hasRole('ORDER_MANAGER')")` - Also an internal, service-to-service call.
*   `POST /products/{productId}/commit`: `@PreAuthorize("hasRole('ORDER_MANAGER')")` - Also an internal, service-to-service call.
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
