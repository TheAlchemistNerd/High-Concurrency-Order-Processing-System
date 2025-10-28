package com.ecommerce.orderprocessing.payment.service;

import com.ecommerce.orderprocessing.payment.client.PaymentGatewayClient;
import com.ecommerce.orderprocessing.payment.dto.*;
import com.ecommerce.orderprocessing.payment.exception.PaymentProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PaymentService {

    private final PaymentGatewayClient paymentGatewayClient;

    public PaymentService(Map<String, PaymentGatewayClient> paymentGatewayClients,
                          @Value("${app.payment.provider:mock}") String paymentProvider) {
        this.paymentGatewayClient = paymentGatewayClients.get(paymentProvider);
    }

    public CompletableFuture<PaymentResponse> processPayment(PaymentRequest paymentRequest) {
        log.info("Processing payment for order: {} with amount: {}",
                paymentRequest.orderId(), paymentRequest.amount());
        return paymentGatewayClient.processPayment(paymentRequest)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Payment processing failed for order: {}", paymentRequest.orderId(), ex);
                        throw new PaymentProcessingException("Payment gateway error: " + ex.getMessage());
                    }
                    if ("SUCCESS".equals(response.status())) {
                        log.info("Payment successful for order: {} - Payment ID: {}",
                                paymentRequest.orderId(), response.paymentId());
                    } else {
                        log.warn("Payment failed for order: {} - Reason: {}",
                                paymentRequest.orderId(), response.message());
                    }
                    return response;
                });
    }

    public CompletableFuture<RefundResponse> refundPayment(RefundRequest request) {
        log.info("Processing refund for payment: {} with amount: {}", request.paymentId(), request.amount());
        return paymentGatewayClient.refundPayment(request)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Refund processing failed for payment: {}", request.paymentId(), ex);
                        throw new PaymentProcessingException("Refund processing failed: " + ex.getMessage());
                    }
                    if ("SUCCESS".equals(response.status())) {
                        log.info("Refund successful for payment: {} - Refund ID: {}",
                                request.paymentId(), response.refundId());
                    } else {
                        log.warn("Refund failed for payment: {} - Reason: {}",
                                request.paymentId(), response.message());
                    }
                    return response;
                });
    }

    public CompletableFuture<PaymentStatusResponse> getPaymentStatus(String transactionId) {
        log.debug("Getting payment status for transaction: {}", transactionId);
        return paymentGatewayClient.getPaymentStatus(transactionId)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Failed to get payment status for transaction: {}", transactionId, ex);
                        throw new PaymentProcessingException("Failed to retrieve payment status: " + ex.getMessage());
                    }
                    return response;
                });
    }

    public CompletableFuture<AuthorizationResponse> authorizePayment(AuthorizationRequest request) {
        log.info("Authorizing payment for order: {} with amount: {}", request.orderId(), request.amount());
        return paymentGatewayClient.authorizePayment(request)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Authorization failed for order: {}", request.orderId(), ex);
                        throw new PaymentProcessingException("Payment authorization error: " + ex.getMessage());
                    }
                    log.info("Authorization {} for order: {} - Auth ID: {}", response.status(), request.orderId(), response.authorizationId());
                    return response;
                });
    }

    public CompletableFuture<CaptureResponse> capturePayment(CaptureRequest request) {
        log.info("Capturing payment for authorization: {} with amount: {}", request.authorizationId(), request.amount());
        return paymentGatewayClient.capturePayment(request)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Capture failed for authorization: {}", request.authorizationId(), ex);
                        throw new PaymentProcessingException("Payment capture error: " + ex.getMessage());
                    }
                    log.info("Capture {} for authorization: {} - Capture ID: {}", response.status(), request.authorizationId(), response.captureId());
                    return response;
                });
    }

    public CompletableFuture<VoidResponse> voidPayment(VoidRequest request) {
        log.info("Voiding authorization: {}", request.authorizationId());
        return paymentGatewayClient.voidPayment(request)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Void failed for authorization: {}", request.authorizationId(), ex);
                        throw new PaymentProcessingException("Payment void error: " + ex.getMessage());
                    }
                    log.info("Void {} for authorization: {}", response.status(), request.authorizationId());
                    return response;
                });
    }

    public CompletableFuture<CustomerResponse> createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer: {}", request.email());
        return paymentGatewayClient.createCustomer(request)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Customer creation failed for email: {}", request.email(), ex);
                        throw new PaymentProcessingException("Customer creation error: " + ex.getMessage());
                    }
                    log.info("Customer created successfully for email: {} - Gateway Customer ID: {}", request.email(), response.gatewayCustomerId());
                    return response;
                });
    }

    public CompletableFuture<PaymentMethodResponse> addPaymentMethod(AddPaymentMethodRequest request) {
        log.info("Adding payment method for customer: {}", request.customerId());
        return paymentGatewayClient.addPaymentMethod(request)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Adding payment method failed for customer: {}", request.customerId(), ex);
                        throw new PaymentProcessingException("Add payment method error: " + ex.getMessage());
                    }
                    log.info("Payment method added successfully for customer: {} - Payment Method ID: {}", request.customerId(), response.paymentMethodId());
                    return response;
                });
    }

    public CompletableFuture<List<Transaction>> listTransactions(ListTransactionsRequest request) {
        log.info("Listing transactions for customer: {}", request.customerId());
        return paymentGatewayClient.listTransactions(request)
                .handle((response, ex) -> {
                    if (ex != null) {
                        log.error("Listing transactions failed for customer: {}", request.customerId(), ex);
                        throw new PaymentProcessingException("List transactions error: " + ex.getMessage());
                    }
                    log.info("Successfully listed {} transactions for customer: {}", response != null ? response.size() : 0, request.customerId());
                    return response;
                });
    }
}
