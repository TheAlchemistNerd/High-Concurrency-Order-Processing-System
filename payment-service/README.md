# Payment Service Documentation

## 1. Introduction

The Payment Service is a critical **internal** component responsible for handling all financial transactions within the e-commerce platform. It provides an abstraction layer for interacting with various external payment gateways, ensuring that the core business logic remains decoupled from specific provider implementations. This service is designed to be consumed by other internal services (e.g., Order Service) and does not expose direct public API endpoints.

## 2. Key Features

*   **Payment Gateway Abstraction**: Decouples core application logic from specific payment provider APIs (e.g., Stripe, PayPal).
*   **Payment Processing**: Initiates and manages payment transactions.
*   **Refund Processing**: Handles the refunding of payments.
*   **Payment Status Inquiry**: Allows querying the status of a payment transaction for internal reconciliation.
*   **Extensible Design**: Easily integrate new payment gateways with minimal changes.

## 3. Internal Service Methods

This service exposes methods to other internal services, primarily the Order Service. These methods are not directly accessible via REST endpoints.

*   `CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest)`
*   `CompletableFuture<RefundResponse> refundPayment(RefundRequest request)`
*   `CompletableFuture<PaymentStatusResponse> getPaymentStatus(String transactionId)`
*   `CompletableFuture<AuthorizationResponse> authorizePayment(AuthorizationRequest request)`
*   `CompletableFuture<CaptureResponse> capturePayment(CaptureRequest request)`
*   `CompletableFuture<VoidResponse> voidPayment(VoidRequest request)`
*   `CompletableFuture<CustomerResponse> createCustomer(CreateCustomerRequest request)`
*   `CompletableFuture<PaymentMethodResponse> addPaymentMethod(AddPaymentMethodRequest request)`
*   `CompletableFuture<List<Transaction>> listTransactions(ListTransactionsRequest request)`

## 4. Data Transfer Objects (DTOs)

### `PaymentRequest`
Request body for processing a new payment.
*   `orderId`: Long (required) - The ID of the order associated with the payment.
*   `paymentMethod`: String (required) - The payment method used (e.g., "credit_card", "paypal").
*   `amount`: BigDecimal (required, min 0.01) - The amount to be paid.
*   `currency`: String (required, 3 characters) - The currency of the payment (e.g., "USD").
*   `cardNumber`: String - (Sensitive) The credit card number. (In a real system, this would be handled client-side via tokenization).
*   `cardHolderName`: String - The name of the cardholder.
*   `expiryMonth`: String - The expiry month of the card.
*   `expiryYear`: String - The expiry year of the card.
*   `cvv`: String - The CVV of the card.

### `PaymentResponse`
Response body after processing a payment.
*   `paymentId`: String - Unique identifier for the payment transaction from the gateway.
*   `status`: String - The status of the payment (e.g., "SUCCESS", "FAILED").
*   `amount`: BigDecimal - The amount processed.
*   `currency`: String - The currency of the payment.
*   `paymentMethod`: String - The payment method used.
*   `processedAt`: LocalDateTime - Timestamp of when the payment was processed.
*   `transactionId`: String - Internal transaction ID.
*   `message`: String - A descriptive message about the payment outcome.

### `RefundRequest`
Request body for initiating a refund.
*   `paymentId`: String (required) - The ID of the original payment to be refunded.
*   `amount`: BigDecimal (required, min 0.01) - The amount to refund.
*   `reason`: String - The reason for the refund.
*   `idempotencyKey`: String - A unique key to ensure the refund operation is processed only once.

### `RefundResponse`
Response body after processing a refund.
*   `refundId`: String - Unique identifier for the refund transaction from the gateway.
*   `paymentId`: String - The ID of the original payment.
*   `status`: String - The status of the refund (e.g., "SUCCESS", "FAILED").
*   `amount`: BigDecimal - The amount refunded.
*   `currency`: String - The currency of the refund.
*   `processedAt`: LocalDateTime - Timestamp of when the refund was processed.
*   `message`: String - A descriptive message about the refund outcome.

### `PaymentStatusResponse`
Response body for querying the status of a payment transaction.
*   `paymentId`: String - Unique identifier for the payment transaction.
*   `status`: String - The high-level status (e.g., "SUCCESS", "PENDING", "FAILED").
*   `detailedStatus`: String - A more granular status from the gateway.
*   `lastUpdated`: LocalDateTime - Timestamp of the last status update.
*   `message`: String - A descriptive message about the status.

### `AuthorizationRequest`
Request body for authorizing a payment (holding funds).
*   `orderId`: Long (required) - The ID of the order.
*   `paymentMethod`: String (required) - The payment method.
*   `amount`: BigDecimal (required, min 0.01) - The amount to authorize.
*   `currency`: String (required, 3 characters) - The currency.
*   `customerId`: String - The customer ID at the payment gateway.
*   `paymentMethodToken`: String - A token representing the payment method.
*   `idempotencyKey`: String - Unique key for idempotency.

### `AuthorizationResponse`
Response body after authorizing a payment.
*   `authorizationId`: String - Unique identifier for the authorization.
*   `status`: String - Status of the authorization (e.g., "AUTHORIZED", "DECLINED").
*   `amount`: BigDecimal - The authorized amount.
*   `currency`: String - The currency.
*   `authorizedAt`: LocalDateTime - Timestamp of authorization.
*   `message`: String - Descriptive message.

### `CaptureRequest`
Request body for capturing previously authorized funds.
*   `authorizationId`: String (required) - The ID of the authorization to capture.
*   `amount`: BigDecimal (required, min 0.01) - The amount to capture.
*   `idempotencyKey`: String - Unique key for idempotency.

### `CaptureResponse`
Response body after capturing funds.
*   `captureId`: String - Unique identifier for the capture.
*   `authorizationId`: String - The ID of the original authorization.
*   `status`: String - Status of the capture (e.g., "CAPTURED", "FAILED").
*   `amount`: BigDecimal - The captured amount.
*   `currency`: String - The currency.
*   `capturedAt`: LocalDateTime - Timestamp of capture.
*   `message`: String - Descriptive message.

### `VoidRequest`
Request body for voiding an authorization.
*   `authorizationId`: String (required) - The ID of the authorization to void.
*   `reason`: String - Reason for voiding.
*   `idempotencyKey`: String - Unique key for idempotency.

### `VoidResponse`
Response body after voiding an authorization.
*   `authorizationId`: String - The ID of the voided authorization.
*   `status`: String - Status of the void operation (e.g., "VOIDED", "FAILED").
*   `voidedAt`: LocalDateTime - Timestamp of void.
*   `message`: String - Descriptive message.

### `CreateCustomerRequest`
Request body for creating a customer at the payment gateway.
*   `customerId`: String (required) - Our internal customer ID.
*   `name`: String (required) - Customer's name.
*   `email`: String (required, valid email) - Customer's email.

### `CustomerResponse`
Response body after creating a customer at the payment gateway.
*   `customerId`: String - Our internal customer ID.
*   `gatewayCustomerId`: String - The customer ID assigned by the payment gateway.
*   `name`: String - Customer's name.
*   `email`: String - Customer's email.
*   `message`: String - Descriptive message.

### `AddPaymentMethodRequest`
Request body for adding a payment method for a customer.
*   `customerId`: String (required) - Our internal customer ID.
*   `paymentMethodType`: String (required) - Type of payment method (e.g., "card").
*   `token`: String (required) - Client-side generated token representing the payment method.
*   `cardLastFour`: String - Last four digits of the card.
*   `cardBrand`: String - Brand of the card (e.g., "Visa").
*   `cardExpMonth`: Integer - Expiry month of the card.
*   `cardExpYear`: Integer - Expiry year of the card.

### `PaymentMethodResponse`
Response body after adding a payment method.
*   `paymentMethodId`: String - Unique ID for the payment method from the gateway.
*   `customerId`: String - Our internal customer ID.
*   `type`: String - Type of payment method.
*   `lastFour`: String - Last four digits of the card.
*   `brand`: String - Brand of the card.
*   `expMonth`: Integer - Expiry month.
*   `expYear`: Integer - Expiry year.
*   `isDefault`: boolean - Whether this is the default payment method.
*   `message`: String - Descriptive message.

### `ListTransactionsRequest`
Request body for listing transactions.
*   `customerId`: String - Filter by customer ID.
*   `startDate`: LocalDateTime - Start date for transaction range.
*   `endDate`: LocalDateTime - End date for transaction range.
*   `status`: String - Filter by transaction status.
*   `limit`: Integer - Maximum number of transactions to return.
*   `offset`: Integer - Offset for pagination.

### `Transaction`
Represents a single transaction record.
*   `transactionId`: String - Unique ID for the transaction.
*   `paymentId`: String - ID of the associated payment.
*   `type`: String - Type of transaction (e.g., "CHARGE", "REFUND").
*   `status`: String - Status of the transaction.
*   `amount`: BigDecimal - Amount of the transaction.
*   `currency`: String - Currency.
*   `transactionDate`: LocalDateTime - Date and time of the transaction.
*   `description`: String - Description of the transaction.