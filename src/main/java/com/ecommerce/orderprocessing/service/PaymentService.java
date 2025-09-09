package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.dto.request.PaymentRequest;
import com.ecommerce.orderprocessing.dto.response.PaymentResponse;
import com.ecommerce.orderprocessing.exception.PaymentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentGatewayClient paymentGatewayClient;

    public PaymentService(PaymentGatewayClient paymentGatewayClient) {
        this.paymentGatewayClient = paymentGatewayClient;
    }

    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        logger.info("Processing payment for order: {} with amount: {}",
                paymentRequest.orderId(), paymentRequest.amount());
        return paymentGatewayClient.processPayment(paymentRequest)
                .handle((response, ex) -> {
                    if (ex != null) {
                        logger.error("Payment processing failed for order: {}", paymentRequest.orderId(), ex);
                        throw new PaymentProcessingException("Payment gateway error: " + ex.getMessage());
                    }
                    if ("SUCCESS".equals(response.status())) {
                        logger.info("Payment successful for order: {} - Payment ID: {}",
                                paymentRequest.orderId(), response.paymentId());
                    } else {
                        logger.warn("Payment failed for order: {} - Reason: {}",
                                paymentRequest.orderId(), response.message());
                    }
                    return response;
                });
    }

    public CompletableFuture<PaymentResponse> refundPayment(String paymentId, BigDecimal amount) {
        logger.info("Processing refund for payment: {} with amount: {}", paymentId, amount);
        return paymentGatewayClient.refundPayment(paymentId, amount)
                .handle((response, ex) -> {
                    if (ex != null) {
                        logger.error("Refund processing failed for payment: {}", paymentId, ex);
                        throw new PaymentProcessingException("Refund processing failed: " + ex.getMessage());
                    }
                    if ("SUCCESS".equals(response.status())) {
                        logger.info("Refund successful for payment: {}", paymentId);
                    } else {
                        logger.warn("Refund failed for payment: {} - Reason: {}", paymentId, response.message());
                    }
                    return response;
                });
    }

    public CompletableFuture<PaymentResponse> getPaymentStatus(String paymentId) {
        logger.debug("Getting payment status for payment: {}", paymentId);
        return paymentGatewayClient.getPaymentStatus(paymentId)
                .handle((response, ex) -> {
                    if (ex != null) {
                        logger.error("Failed to get payment status for payment: {}", paymentId, ex);
                        throw new PaymentProcessingException("Failed to retrieve payment status: " + ex.getMessage());
                    }
                    return response;
                });
    }
}

