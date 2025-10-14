package com.ecommerce.orderprocessing.payment.service;

import com.ecommerce.orderprocessing.payment.PaymentRequest;
import com.ecommerce.orderprocessing.payment.PaymentResponse;
import com.ecommerce.orderprocessing.payment.PaymentProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGatewayClient paymentGatewayClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentGatewayClient);
    }

    @Test
    void processPayment_shouldReturnPaymentResponse() throws Exception {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Payment successful");
        when(paymentGatewayClient.processPayment(any(PaymentRequest.class))).thenReturn(CompletableFuture.completedFuture(paymentResponse));

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
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        when(paymentGatewayClient.processPayment(any(PaymentRequest.class))).thenReturn(CompletableFuture.failedFuture(new RuntimeException("Gateway error")));

        // When
        CompletableFuture<PaymentResponse> future = paymentService.processPayment(paymentRequest);

        // Then
        assertThatThrownBy(future::get).hasCauseInstanceOf(PaymentProcessingException.class);
    }

    @Test
    void refundPayment_shouldReturnPaymentResponse() throws Exception {
        // Given
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Refund successful");
        when(paymentGatewayClient.refundPayment(any(), any())).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        // When
        CompletableFuture<PaymentResponse> future = paymentService.refundPayment("payment-1", BigDecimal.TEN);
        PaymentResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
    }

    @Test
    void getPaymentStatus_shouldReturnPaymentResponse() throws Exception {
        // Given
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Payment completed");
        when(paymentGatewayClient.getPaymentStatus(any())).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        // When
        CompletableFuture<PaymentResponse> future = paymentService.getPaymentStatus("payment-1");
        PaymentResponse response = future.get();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
    }
}
