package com.ecommerce.orderprocessing.payment.service;

import com.ecommerce.orderprocessing.payment.client.PaymentGatewayClient;
import com.ecommerce.orderprocessing.payment.dto.*;
import com.ecommerce.orderprocessing.payment.exception.PaymentProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGatewayClient mockPaymentGatewayClient;

    @Mock
    private PaymentGatewayClient stripePaymentGatewayClient;

    private PaymentService paymentService;

    private Map<String, PaymentGatewayClient> paymentGatewayClients;

    @BeforeEach
    void setUp() {
        paymentGatewayClients = new HashMap<>();
        paymentGatewayClients.put("mock", mockPaymentGatewayClient);
        paymentGatewayClients.put("stripe", stripePaymentGatewayClient);

        // Default provider is 'mock' as per application.properties
        paymentService = new PaymentService(paymentGatewayClients, "mock");
    }

    @Test
    void processPayment_shouldReturnPaymentResponse() throws Exception {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "mock", BigDecimal.TEN, "USD", "123", "name", "12", "2025", "123");
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "mock", LocalDateTime.now(), "trx-1", "Payment successful");
        when(mockPaymentGatewayClient.processPayment(any(PaymentRequest.class))).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        // When
        CompletableFuture<PaymentResponse> future = paymentService.processPayment(paymentRequest);
        PaymentResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
    }

    @Test
    void processPayment_whenGatewayFails_shouldThrowException() {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "mock", BigDecimal.TEN, "USD", "123", "name", "12", "2025", "123");
        when(mockPaymentGatewayClient.processPayment(any(PaymentRequest.class))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Gateway error")));

        // When
        CompletableFuture<PaymentResponse> future = paymentService.processPayment(paymentRequest);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(PaymentProcessingException.class);
    }

    @Test
    void refundPayment_shouldReturnRefundResponse() throws Exception {
        // Given
        RefundRequest refundRequest = new RefundRequest("payment-1", BigDecimal.TEN, "reason", "idempotency-key");
        RefundResponse refundResponse = new RefundResponse("refund-1", "payment-1", "SUCCESS", BigDecimal.TEN, "USD", LocalDateTime.now(), "Refund successful");
        when(mockPaymentGatewayClient.refundPayment(any(RefundRequest.class))).thenReturn(CompletableFuture.completedFuture(refundResponse));

        // When
        CompletableFuture<RefundResponse> future = paymentService.refundPayment(refundRequest);
        RefundResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
    }

    @Test
    void getPaymentStatus_shouldReturnPaymentStatusResponse() throws Exception {
        // Given
        PaymentStatusResponse paymentStatusResponse = new PaymentStatusResponse("payment-1", "SUCCESS", "COMPLETED", LocalDateTime.now(), "Payment completed");
        when(mockPaymentGatewayClient.getPaymentStatus(anyString())).thenReturn(CompletableFuture.completedFuture(paymentStatusResponse));

        // When
        CompletableFuture<PaymentStatusResponse> future = paymentService.getPaymentStatus("payment-1");
        PaymentStatusResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
    }

    @Test
    void authorizePayment_shouldReturnAuthorizationResponse() throws Exception {
        // Given
        AuthorizationRequest authRequest = new AuthorizationRequest(1L, "mock", BigDecimal.valueOf(100.00), "USD", "cust1", "pm_token", "idempotency-key");
        AuthorizationResponse authResponse = new AuthorizationResponse("auth-1", "AUTHORIZED", BigDecimal.valueOf(100.00), "USD", LocalDateTime.now(), "Auth successful");
        when(mockPaymentGatewayClient.authorizePayment(any(AuthorizationRequest.class))).thenReturn(CompletableFuture.completedFuture(authResponse));

        // When
        CompletableFuture<AuthorizationResponse> future = paymentService.authorizePayment(authRequest);
        AuthorizationResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("AUTHORIZED");
    }

    @Test
    void capturePayment_shouldReturnCaptureResponse() throws Exception {
        // Given
        CaptureRequest captureRequest = new CaptureRequest("auth-1", BigDecimal.valueOf(100.00), "idempotency-key");
        CaptureResponse captureResponse = new CaptureResponse("cap-1", "auth-1", "CAPTURED", BigDecimal.valueOf(100.00), "USD", LocalDateTime.now(), "Capture successful");
        when(mockPaymentGatewayClient.capturePayment(any(CaptureRequest.class))).thenReturn(CompletableFuture.completedFuture(captureResponse));

        // When
        CompletableFuture<CaptureResponse> future = paymentService.capturePayment(captureRequest);
        CaptureResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("CAPTURED");
    }

    @Test
    void voidPayment_shouldReturnVoidResponse() throws Exception {
        // Given
        VoidRequest voidRequest = new VoidRequest("auth-1", "reason", "idempotency-key");
        VoidResponse voidResponse = new VoidResponse("auth-1", "VOIDED", LocalDateTime.now(), "Void successful");
        when(mockPaymentGatewayClient.voidPayment(any(VoidRequest.class))).thenReturn(CompletableFuture.completedFuture(voidResponse));

        // When
        CompletableFuture<VoidResponse> future = paymentService.voidPayment(voidRequest);
        VoidResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("VOIDED");
    }

    @Test
    void createCustomer_shouldReturnCustomerResponse() throws Exception {
        // Given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest("cust1", "John Doe", "john.doe@example.com");
        CustomerResponse customerResponse = new CustomerResponse("cust1", "gateway-cust-1", "John Doe", "john.doe@example.com", "Customer created");
        when(mockPaymentGatewayClient.createCustomer(any(CreateCustomerRequest.class))).thenReturn(CompletableFuture.completedFuture(customerResponse));

        // When
        CompletableFuture<CustomerResponse> future = paymentService.createCustomer(createCustomerRequest);
        CustomerResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.gatewayCustomerId()).isEqualTo("gateway-cust-1");
    }

    @Test
    void addPaymentMethod_shouldReturnPaymentMethodResponse() throws Exception {
        // Given
        AddPaymentMethodRequest addPaymentMethodRequest = new AddPaymentMethodRequest("cust1", "card", "token", "1234", "Visa", 12, 2025);
        PaymentMethodResponse paymentMethodResponse = new PaymentMethodResponse("pm-1", "cust1", "card", "1234", "Visa", 12, 2025, true, "PM added");
        when(mockPaymentGatewayClient.addPaymentMethod(any(AddPaymentMethodRequest.class))).thenReturn(CompletableFuture.completedFuture(paymentMethodResponse));

        // When
        CompletableFuture<PaymentMethodResponse> future = paymentService.addPaymentMethod(addPaymentMethodRequest);
        PaymentMethodResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.paymentMethodId()).isEqualTo("pm-1");
    }

    @Test
    void listTransactions_shouldReturnListOfTransactions() throws Exception {
        // Given
        ListTransactionsRequest listTransactionsRequest = new ListTransactionsRequest("cust1", null, null, null, 10, 0);
        List<Transaction> transactions = Collections.singletonList(new Transaction("trx-1", "pay-1", "CHARGE", "SUCCESS", BigDecimal.TEN, "USD", LocalDateTime.now(), "desc"));
        when(mockPaymentGatewayClient.listTransactions(any(ListTransactionsRequest.class))).thenReturn(CompletableFuture.completedFuture(transactions));

        // When
        CompletableFuture<List<Transaction>> future = paymentService.listTransactions(listTransactionsRequest);
        List<Transaction> response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
    }
}
