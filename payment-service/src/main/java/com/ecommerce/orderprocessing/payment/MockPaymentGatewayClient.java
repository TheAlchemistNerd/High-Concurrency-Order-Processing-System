package com.ecommerce.orderprocessing.payment;

import com.ecommerce.orderprocessing.common.exception.ExternalServiceException;
import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public CompletableFuture<PaymentResponse> refundPayment(String paymentId, BigDecimal amount) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Processing (mock) refund for payment: {}", paymentId);
            try {
                Thread.sleep(100);
                return new PaymentResponse(
                        paymentId,
                        "SUCCESS",
                        amount,
                        "USD",
                        null,
                        LocalDateTime.now(),
                        "refund_" + System.currentTimeMillis(),
                        "Refund processed successfully"
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Refund processing interrupted");
            }
        }, virtualThreadExecutor);
    }

    @Override
    public CompletableFuture<PaymentResponse> getPaymentStatus(String paymentId) {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Getting (mock) payment status for: {}", paymentId);
            try {
                Thread.sleep(50);
                return new PaymentResponse(
                        paymentId,
                        "SUCCESS",
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        null,
                        "Payment completed"
                );
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ExternalServiceException("PaymentGateway", "Status check interrupted");
            }
        }, virtualThreadExecutor);
    }
}
