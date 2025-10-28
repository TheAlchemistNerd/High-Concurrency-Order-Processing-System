# Design Decisions: Edge Cases and Administrative Actions

This document captures design decisions and considerations regarding edge cases and administrative actions within the e-commerce platform, particularly concerning payment and order processing flows. These decisions aim to balance functionality with security and implementation complexity, especially within the context of a modular monolith.

## 1. Current Design Overview

*   **Payment Processing (`paymentService.processPayment`)**: Primarily initiated within the `OrderService`'s `processOrderPayment` method. Exposed via `OrderController.processOrderPayment`, secured with `@PreAuthorize("hasRole('CUSTOMER')")`.
    *   **Implication**: Payment processing is currently designed to be strictly handled within an order flow, and specifically, initiated by the customer themselves.
*   **Refunds (`paymentService.refundPayment`)**: Initiated within the `OrderService`'s `cancelOrder` method. The `cancelOrder` endpoint is secured with `@PreAuthorize("hasAnyRole('ADMIN', 'ORDER_MANAGER') or ...")`.
    *   **Implication**: Refunds are handled within the order cancellation flow, and can be initiated by `ADMIN`, `ORDER_MANAGER`, or the `CUSTOMER` who owns the order.

## 2. Roles Available

*   `ROLE_CUSTOMER`: Standard registered user. Can manage their own profile, cart, and orders.
*   `ROLE_ADMIN`: Superuser with wide-ranging permissions.
*   `ROLE_PRODUCT_MANAGER`: Manages the product catalog and inventory.
*   `ROLE_ORDER_MANAGER`: Manages orders (status updates, cancellations, refunds).
*   `ROLE_SUPPORT`: Read-only access to customer/order data for assistance.

## 3. Administrative Actions: Processing Payments on Behalf of Customers

**Scenario**: An `ORDER_MANAGER` wants to help a customer process payments by placing an order for them.

**Current Design Limitation**: An `ORDER_MANAGER` currently *cannot* directly place an order or process a payment on behalf of a customer through the existing API endpoints, as `OrderController.createOrder` and `OrderController.processOrderPayment` are restricted to `ROLE_CUSTOMER`.

**Design Decision (Chosen Approach)**:
To address this, we will adopt a pragmatic approach that balances immediate needs with avoiding complex security flows or abuse-prone mechanisms like impersonation. We will **modify the existing `OrderController` endpoints to allow `ORDER_MANAGER`s to act on behalf of customers for order creation and payment processing.**

*   **Change**: Modify the `@PreAuthorize` annotations on `OrderController.createOrder` and `OrderController.processOrderPayment` to include `hasAnyRole('CUSTOMER', 'ORDER_MANAGER')`.
*   **Rationale**: This is the quickest to implement and directly addresses the business need without introducing complex impersonation flows or dedicated admin endpoints that might require significant security overhead. It acknowledges that `ORDER_MANAGER`s are trusted internal users.
*   **Mitigation**: The `OrderService` will be responsible for robust logging and audit trails. It must log *who* initiated the action (the `ORDER_MANAGER`) and *for whom* (the `customerId` provided in the request). This ensures accountability.

## 4. Other Edge Cases and Design Considerations

### 4.1. Payment Failure during Order Creation (Customer Initiated)
*   **Design**: The `OrderService` will explicitly handle payment failures by updating the order status to `PAYMENT_FAILED` (assuming this status is added to the `OrderStatus` enum). The system will allow the customer to retry payment for orders in this state.

### 4.2. Reconciliation of Payments (Internal)
*   **Design**: Internal administrative tools or scheduled jobs will utilize the `PaymentService`'s internal methods (`getPaymentStatus`, `listTransactions`) to reconcile payment statuses with the `OrderService`'s records. This is crucial for detecting and resolving discrepancies.

### 4.3. Chargebacks/Asynchronous Payment Events (Webhooks)
*   **Design**: As a future enhancement, the `payment-service` will implement an internal mechanism (e.g., a message listener or a dedicated internal endpoint) to receive webhooks from external payment gateways for events like chargebacks or successful settlements. These webhooks will be translated into internal commands or events, which the `OrderService` (or a dedicated `TransactionService`) will process to update order/payment statuses accordingly. This forms a critical part of a robust Saga pattern implementation.

### 4.4. Partial Refunds
*   **Design**: The `OrderService`'s refund logic will support partial refunds by correctly determining the refund amount and passing it in the `RefundRequest`. The `Order` entity will be extended to track refunded amounts.

## 5. Overall Design Principles for Modular Monolith

*   **Clear Internal APIs**: Services expose clear public interfaces (Java interfaces or well-documented classes) for other internal services to consume. This is exemplified by the `PaymentService`.
*   **Orchestration**: The `OrderService` acts as the primary orchestrator for complex business processes spanning multiple services.
*   **Auditability**: Robust logging and audit trails are essential for all administrative actions, tracking who performed an action and on whose behalf.
*   **Error Handling and Compensation**: The system is designed for failures, with compensating actions (e.g., inventory release on order cancellation) implemented to maintain consistency.
