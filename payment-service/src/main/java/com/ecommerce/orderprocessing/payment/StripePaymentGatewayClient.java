package com.ecommerce.orderprocessing.payment;

import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service("stripe")
public class StripePaymentGatewayClient implements PaymentGatewayClient {
    @Override
    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        // To be implemented
        return null;
    }

    @Override
    public CompletableFuture<PaymentResponse> refundPayment(String paymentId, BigDecimal amount) {
        // To be implemented
        return null;
    }

    @Override
    public CompletableFuture<PaymentResponse> getPaymentStatus(String paymentId) {
        // To be implemented
        return null;
    }
}
