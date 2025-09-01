# Architectural Vision: Advanced User Profile Management

## Introduction

A rich and seamless user profile management system is a critical component of any modern e-commerce platform. It enhances the customer experience, builds trust, and provides valuable data for personalization. The current system implements basic customer registration and authentication, but there is a significant opportunity to expand this into a full-fledged, feature-rich user profile system. This document outlines the architectural vision for creating a dedicated **User Profile Service**, evolving the existing `Customer` entity into a comprehensive domain.

## 1. Core Profile Components & Expanded Data Model

To move beyond basic authentication, the concept of a user profile should be enriched to include a variety of data points that facilitate a smoother user journey. The data model should be expanded to encompass:

*   **Core Personal Information**: Beyond the current `name` and `email`, this should include:
    *   First Name / Last Name (separated for personalization)
    *   Profile Picture URL
    *   Contact Phone Number

*   **Address Management**: A crucial feature for e-commerce. Users should be able to manage multiple addresses.
    *   A dedicated `Address` entity with fields like `street`, `city`, `state`, `postalCode`, and `country`.
    *   A one-to-many relationship between a User and their Addresses.
    *   The ability to flag addresses as `default_shipping` or `default_billing`.

*   **User Preferences**: To allow for personalization and control.
    *   `communication_preferences`: A structured object to manage user consent for newsletters, promotional emails, and SMS notifications.
    *   `language_preference`: The user's preferred language for site interaction and communication.
    *   `theme_preference`: A choice between 'light' or 'dark' mode for the user interface.

*   **Payment Method Management**: Securely managing payment options is essential for building trust and enabling quick checkouts.
    *   This system should **not** store raw credit card information directly. Instead, it should integrate with a PCI-compliant payment gateway (like Stripe or Braintree).
    *   The profile would store non-sensitive representations of payment methods, such as a `payment_method_token`, the last four digits of a card, the card type (e.g., Visa), and its expiration date.

## 2. Key Features and Proposed API Design

A robust user profile system should offer a suite of features accessible through a secure and intuitive API. The primary resource should be `/api/users/me`, which refers to the currently authenticated user, ensuring users can only access their own data.

*   **View and Edit Profile**:
    *   `GET /api/users/me`: Fetches the complete profile of the logged-in user.
    *   `PUT /api/users/me`: Allows the user to update their core personal information.

*   **Manage Addresses**:
    *   `GET /api/users/me/addresses`: Retrieves all saved addresses for the user.
    *   `POST /api/users/me/addresses`: Adds a new address to the user's profile.
    *   `PUT /api/users/me/addresses/{addressId}`: Updates an existing address.
    *   `DELETE /api/users/me/addresses/{addressId}`: Removes an address.

*   **View Order History**:
    *   `GET /api/users/me/orders`: This endpoint would not be directly served by the User Service. Instead, it would be a proxy or redirect to the `OrderService` (`GET /api/orders/customer/{customerId}`), demonstrating inter-service communication.

*   **Manage Security Credentials**:
    *   `POST /api/users/me/change-password`: A dedicated, secure endpoint for updating a user's password, which should require the current password for verification.

*   **Account Deletion**:
    *   `DELETE /api/users/me`: Provides a mechanism for users to permanently delete their account and associated personal data, a key requirement for regulations like GDPR and CCPA.

## 3. Architectural Implementation: A Dedicated User Service

To properly implement this vision, a dedicated **User Service** (also referred to as a Customer Service) is the ideal architectural approach. This aligns perfectly with the proposed microservices decomposition outlined in the project's `README.md`.

*   **Clear Ownership**: This service would be the exclusive owner of all user-related data, including profiles, addresses, and preferences. The current `CustomerRepository` would be moved entirely within this service's boundary.

*   **Decoupling**: Other services that require user data would no longer access the database directly. Instead, they would communicate with the User Service via its public REST API.
    *   **Example**: When a new order is created, the `OrderService` would make an API call to `GET /api/users/{userId}/addresses` to retrieve the customer's default shipping address, rather than performing a database join.

*   **Authentication and Authorization**: The User Service would be a resource server, protected by the existing JWT authentication scheme. It would validate incoming tokens and use the `userId` within the token to authorize data access, ensuring a user can only ever interact with their own profile.

## 4. Security and Compliance Considerations

Handling user data necessitates a strong focus on security and regulatory compliance.

*   **Data Security**: All Personally Identifiable Information (PII) must be encrypted both in transit (using TLS/SSL) and at rest. Sensitive fields in the database should be encrypted.

*   **PCI Compliance**: As mentioned, direct storage of credit card details must be avoided. The architecture should rely on tokenization provided by a certified payment gateway, which greatly reduces the project's PCI compliance scope and risk.

*   **Password Management**: The system must continue to use a strong, adaptive hashing algorithm like **BCrypt** for all user passwords, as is the current practice with `PasswordEncoderService`.

*   **Regulatory Compliance (GDPR/CCPA)**: The architecture must support key data privacy rights, including:
    *   **The Right to Access**: Fulfilled by the `GET /api/users/me` endpoint.
    *   **The Right to Rectification**: Fulfilled by the `PUT /api/users/me` endpoint.
    *   **The Right to Erasure (to be Forgotten)**: Fulfilled by the `DELETE /api/users/me` endpoint, which must trigger a workflow to scrub the user's PII from the User Service and anonymize their data in other services (like `OrderService`).

## Conclusion

Establishing a dedicated User Profile Service is a strategic investment in the platform's future. It moves beyond a simple login mechanism to create a feature-rich hub for the customer. This approach not only dramatically improves the user experience by providing convenience and control but also enhances the system's overall security, scalability, and maintainability by creating a clean, well-defined boundary for all user-related concerns.
