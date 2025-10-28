package com.ecommerce.orderprocessing.payment.client;

import com.ecommerce.orderprocessing.payment.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service("stripe")
public class StripePaymentGatewayClient implements PaymentGatewayClient {
    @Override
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<RefundResponse> refundPayment(RefundRequest request) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<PaymentStatusResponse> getPaymentStatus(String transactionId) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<AuthorizationResponse> authorizePayment(AuthorizationRequest request) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<CaptureResponse> capturePayment(CaptureRequest request) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<VoidResponse> voidPayment(VoidRequest request) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<CustomerResponse> createCustomer(CreateCustomerRequest request) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<PaymentMethodResponse> addPaymentMethod(AddPaymentMethodRequest request) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<List<Transaction>> listTransactions(ListTransactionsRequest request) {
        // To be implemented
        return CompletableFuture.completedFuture(null);
    }
}
