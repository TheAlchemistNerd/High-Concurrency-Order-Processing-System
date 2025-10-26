package com.ecommerce.orderprocessing.payment;

import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface PaymentGatewayClient {
    CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest);
    CompletableFuture<PaymentResponse> refundPayment(String paymentId, BigDecimal amount);
    CompletableFuture<PaymentResponse> getPaymentStatus(String paymentId);
}
