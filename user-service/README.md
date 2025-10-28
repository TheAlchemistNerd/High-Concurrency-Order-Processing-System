# User Service Documentation

## 1. Introduction

The User Service is a core component of the e-commerce platform responsible for managing all aspects of user identity and profiles. It handles user registration, authentication (local and OAuth2), profile management, address management, and role-based access control (RBAC) information. This service is the authoritative source of truth for all user-related data.

## 2. Key Features

*   **User Registration & Authentication**: Allows new users to register and existing users to authenticate via email/password or OAuth2 providers.
*   **User Profile Management**: Enables users to view and update their personal information.
*   **Address Management**: Provides functionality for users to add, update, and delete multiple addresses.
*   **Password Management**: Securely handles password changes.
*   **Account Deletion**: Allows users to delete their accounts.
*   **Role-Based Access Control (RBAC)**: Integrates with Spring Security to provide granular access control based on user roles (`CUSTOMER`, `ADMIN`, `SUPPORT`).

## 3. API Endpoints

All API endpoints are prefixed with `/api/users`.

### 3.1. User Profile Endpoints

#### `GET /api/users/me`
*   **Description**: Retrieves the profile of the currently authenticated user.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Response**: `200 OK` with `UserResponse` body.

#### `PUT /api/users/me`
*   **Description**: Updates the profile of the currently authenticated user.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Request Body**: `UserProfileUpdateRequest`
*   **Response**: `200 OK` with `UserResponse` body.

#### `DELETE /api/users/me`
*   **Description**: Deletes the account of the currently authenticated user.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Response**: `204 No Content`.

#### `GET /api/users/{userId}`
*   **Description**: Retrieves the profile of a user by their ID. This is an administrative/support endpoint.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`, `ROLE_SUPPORT`.
*   **Path Variable**: `userId` (Long) - The ID of the user to retrieve.
*   **Response**: `200 OK` with `UserResponse` body.

#### `GET /api/users/`
*   **Description**: Retrieves a list of all user profiles. This is an administrative endpoint.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_ADMIN`.
*   **Response**: `200 OK` with a list of `UserResponse` bodies.

### 3.2. Address Management Endpoints

#### `GET /api/users/me/addresses`
*   **Description**: Retrieves all addresses associated with the currently authenticated user.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Response**: `200 OK` with a list of `AddressResponse` bodies.

#### `POST /api/users/me/addresses`
*   **Description**: Adds a new address for the currently authenticated user.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Request Body**: `AddressRequest`
*   **Response**: `201 Created` with `AddressResponse` body.

#### `PUT /api/users/me/addresses/{addressId}`
*   **Description**: Updates an existing address for the currently authenticated user.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Path Variable**: `addressId` (Long) - The ID of the address to update.
*   **Request Body**: `AddressRequest`
*   **Response**: `200 OK` with `AddressResponse` body.

#### `DELETE /api/users/me/addresses/{addressId}`
*   **Description**: Deletes an address for the currently authenticated user.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Path Variable**: `addressId` (Long) - The ID of the address to delete.
*   **Response**: `204 No Content`.

### 3.3. Password Management Endpoints

#### `POST /api/users/me/change-password`
*   **Description**: Changes the password for the currently authenticated user.
*   **Authentication**: Required.
*   **Authorization**: `ROLE_CUSTOMER`.
*   **Request Body**: `ChangePasswordRequest`
*   **Response**: `204 No Content`.

## 4. Data Transfer Objects (DTOs)

### `UserResponse`
Represents the detailed profile information of a user.
*   `id`: Long - Unique identifier of the user.
*   `firstName`: String - User's first name.
*   `lastName`: String - User's last name.
*   `email`: String - User's email address.
*   `phoneNumber`: String - User's phone number.
*   `profilePictureUrl`: String - URL to the user's profile picture.
*   `roles`: String - Comma-separated string of roles assigned to the user.
*   `isActive`: Boolean - Indicates if the user account is active.
*   `createdAt`: LocalDateTime - Timestamp of when the user account was created.

### `UserProfileUpdateRequest`
Request body for updating a user's profile.
*   `firstName`: String (min 2, max 50) - New first name.
*   `lastName`: String (min 2, max 50) - New last name.
*   `phoneNumber`: String (max 20) - New phone number.
*   `profilePictureUrl`: String (max 255) - New profile picture URL.

### `AddressResponse`
Represents a user's address.
*   `id`: Long - Unique identifier of the address.
*   `street`: String - Street address.
*   `city`: String - City.
*   `state`: String - State.
*   `postalCode`: String - Postal code.
*   `country`: String - Country.
*   `isDefaultShipping`: Boolean - Indicates if this is the default shipping address.
*   `isDefaultBilling`: Boolean - Indicates if this is the default billing address.
*   `createdAt`: LocalDateTime - Timestamp of when the address was created.

### `AddressRequest`
Request body for adding or updating an address.
*   `street`: String (required, max 255) - Street address.
*   `city`: String (required, max 100) - City.
*   `state`: String (required, max 100) - State.
*   `postalCode`: String (required, max 20) - Postal code.
*   `country`: String (required, max 100) - Country.
*   `isDefaultShipping`: Boolean - Whether this should be the default shipping address.
*   `isDefaultBilling`: Boolean - Whether this should be the default billing address.

### `ChangePasswordRequest`
Request body for changing a user's password.
*   `currentPassword`: String (required) - The user's current password.
*   `newPassword`: String (required, min 8, max 100, must contain at least one lowercase, one uppercase, and one digit) - The new password.

### `LoginRequest`
Request body for user login.
*   `email`: String (required, valid email format) - User's email address.
*   `password`: String (required) - User's password.

### `LoginResponse`
Response body for successful user login.
*   `token`: String - JWT access token.
*   `tokenType`: String - Type of the token (e.g., "Bearer").
*   `expiresIn`: Long - Token expiration time in seconds.
*   `user`: `UserResponse` - The profile of the logged-in user.

### `UserRegistrationRequest`
Request body for new user registration.
*   `firstName`: String (required, min 2, max 50) - User's first name.
*   `lastName`: String (required, min 2, max 50) - User's last name.
*   `email`: String (required, valid email format, max 100) - User's email address.
*   `password`: String (required, min 8, max 100, must contain at least one lowercase, one uppercase, and one digit) - User's password.
*   `phoneNumber`: String (max 20, valid phone number format) - User's phone number.
