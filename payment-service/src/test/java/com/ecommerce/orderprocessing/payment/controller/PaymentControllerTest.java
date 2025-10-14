package com.ecommerce.orderprocessing.payment.controller;

import com.ecommerce.orderprocessing.payment.PaymentRequest;
import com.ecommerce.orderprocessing.payment.PaymentResponse;
import com.ecommerce.orderprocessing.payment.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationEntryPoint;
import com.ecommerce.orderprocessing.user.security.JwtAuthenticationFilter;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@ExtendWith(SpringExtension.class)
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void processPayment_shouldReturnPaymentResponse() throws Exception {
        // Given
        PaymentRequest paymentRequest = new PaymentRequest(1L, "card", BigDecimal.TEN, "123", "name", "12", "2025", "123");
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Payment successful");
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        // When & Then
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"orderId\": 1, \"paymentMethod\": \"card\", \"amount\": 10, \"currency\": \"USD\" }"))
                .andExpect(status().isOk());
    }

    @Test
    void refundPayment_shouldReturnPaymentResponse() throws Exception {
        // Given
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Refund successful");
        when(paymentService.refundPayment("payment-1", BigDecimal.TEN)).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        // When & Then
        mockMvc.perform(post("/api/payments/payment-1/refunds?amount=10"))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentStatus_shouldReturnPaymentResponse() throws Exception {
        // Given
        PaymentResponse paymentResponse = new PaymentResponse("payment-1", "SUCCESS", BigDecimal.TEN, "USD", "card", LocalDateTime.now(), "trx-1", "Payment completed");
        when(paymentService.getPaymentStatus("payment-1")).thenReturn(CompletableFuture.completedFuture(paymentResponse));

        // When & Then
        mockMvc.perform(get("/api/payments/payment-1"))
                .andExpect(status().isOk());
    }
}

