package com.ecommerce.orderprocessing.payment.client;

import com.ecommerce.orderprocessing.common.exception.ExternalServiceException;
import com.ecommerce.orderprocessing.payment.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service("mock")
public class MockPaymentGatewayClient implements PaymentGatewayClient {

    private final ExecutorService virtualThreadExecutor;

    public MockPaymentGatewayClient(ExecutorService virtualThreadExecutor) {
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    @Override
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Calling (mock) payment gateway for order: {}", paymentRequest.orderId());
            try {
                Thread.sleep(100);
                boolean paymentSuccess = Math.random() < 0.9;
                String status = paymentSuccess ? "SUCCESS" : "FAILED";
                String message = paymentSuccess ? "Payment processed successfully" : "Insufficient funds";

                return new PaymentResponse(
                        "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                        status,
                        paymentRequest.amount(),
                        paymentRequest.currency(),
                        paymentRequest.paymentMethod(),
                        LocalDateTime.now(),
                        "txn_" + System.currentTimeMillis(),
                        message
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Payment processing interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<RefundResponse> refundPayment(RefundRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Processing (mock) refund for payment: {}", request.paymentId());
            try {
                Thread.sleep(100);
                return new RefundResponse(
                        "ref_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                        request.paymentId(),
                        "SUCCESS",
                        request.amount(),
                        "USD",
                        LocalDateTime.now(),
                        "Refund processed successfully"
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Refund processing interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<PaymentStatusResponse> getPaymentStatus(String transactionId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Getting (mock) payment status for: {}", transactionId);
            try {
                Thread.sleep(50);
                return new PaymentStatusResponse(
                        transactionId,
                        "SUCCESS",
                        "Payment completed",
                        LocalDateTime.now(),
                        "Payment completed successfully"
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Status check interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<AuthorizationResponse> authorizePayment(AuthorizationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Authorizing (mock) payment for order: {}", request.orderId());
            try {
                Thread.sleep(100);
                boolean success = Math.random() < 0.9;
                String status = success ? "AUTHORIZED" : "DECLINED";
                String message = success ? "Authorization successful" : "Authorization declined";
                return new AuthorizationResponse(
                        "auth_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                        status,
                        request.amount(),
                        request.currency(),
                        LocalDateTime.now(),
                        message
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Authorization interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<CaptureResponse> capturePayment(CaptureRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Capturing (mock) payment for authorization: {}", request.authorizationId());
            try {
                Thread.sleep(100);
                boolean success = Math.random() < 0.9;
                String status = success ? "CAPTURED" : "FAILED";
                String message = success ? "Capture successful" : "Capture failed";
                return new CaptureResponse(
                        "cap_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                        request.authorizationId(),
                        status,
                        request.amount(),
                        "USD", // Assuming USD for mock
                        LocalDateTime.now(),
                        message
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Capture interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<VoidResponse> voidPayment(VoidRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Voiding (mock) authorization: {}", request.authorizationId());
            try {
                Thread.sleep(100);
                boolean success = Math.random() < 0.9;
                String status = success ? "VOIDED" : "FAILED";
                String message = success ? "Void successful" : "Void failed";
                return new VoidResponse(
                        request.authorizationId(),
                        status,
                        LocalDateTime.now(),
                        message
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Void interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<CustomerResponse> createCustomer(CreateCustomerRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Creating (mock) customer: {}", request.email());
            try {
                Thread.sleep(50);
                return new CustomerResponse(
                        request.customerId(),
                        "cus_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                        request.name(),
                        request.email(),
                        "Customer created successfully"
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Create customer interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<PaymentMethodResponse> addPaymentMethod(AddPaymentMethodRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Adding (mock) payment method for customer: {}", request.customerId());
            try {
                Thread.sleep(50);
                return new PaymentMethodResponse(
                        "pm_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16),
                        request.customerId(),
                        request.paymentMethodType(),
                        request.cardLastFour(),
                        request.cardBrand(),
                        request.cardExpMonth(),
                        request.cardExpYear(),
                        true,
                        "Payment method added successfully"
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Add payment method interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<List<Transaction>> listTransactions(ListTransactionsRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Listing (mock) transactions for customer: {}", request.customerId());
            try {
                Thread.sleep(150);
                // Return a mock list of transactions
                return List.of(
                        new Transaction("txn_1", "pay_1", "CHARGE", "SUCCESS", new BigDecimal("100.00"), "USD", LocalDateTime.now().minusDays(5), "Order #123"),
                        new Transaction("txn_2", "pay_2", "REFUND", "SUCCESS", new BigDecimal("10.00"), "USD", LocalDateTime.now().minusDays(2), "Refund for Order #456")
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "List transactions interrupted");
            }
        }, virtualThreadExecutor);
    }
}
