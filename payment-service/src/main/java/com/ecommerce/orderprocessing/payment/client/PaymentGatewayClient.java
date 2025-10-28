package com.ecommerce.orderprocessing.payment.client;

import com.ecommerce.orderprocessing.payment.dto.*;

import java.util.concurrent.CompletableFuture;
import java.util.List;

public interface PaymentGatewayClient {
    CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest);
    CompletableFuture<RefundResponse> refundPayment(RefundRequest request);
    CompletableFuture<PaymentStatusResponse> getPaymentStatus(String transactionId);

    // Section 2.1: Implementing the Authorize and Capture Flow
    CompletableFuture<AuthorizationResponse> authorizePayment(AuthorizationRequest request);
    CompletableFuture<CaptureResponse> capturePayment(CaptureRequest request);
    CompletableFuture<VoidResponse> voidPayment(VoidRequest request);

    // Section 2.2: Customer and Payment Method Tokenization
    CompletableFuture<CustomerResponse> createCustomer(CreateCustomerRequest request);
    CompletableFuture<PaymentMethodResponse> addPaymentMethod(AddPaymentMethodRequest request);

    // Section 2.3: Enhanced Reporting and Reconciliation
    CompletableFuture<List<Transaction>> listTransactions(ListTransactionsRequest request);
}
