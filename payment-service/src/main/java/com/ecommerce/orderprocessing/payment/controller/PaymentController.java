package com.ecommerce.orderprocessing.payment.controller;

import com.ecommerce.orderprocessing.payment.dto.PaymentRequest;
import com.ecommerce.orderprocessing.payment.dto.PaymentResponse;
import com.ecommerce.orderprocessing.payment.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public CompletableFuture<PaymentResponse> processPayment(@RequestBody PaymentRequest paymentRequest) {
        return paymentService.processPayment(paymentRequest);
    }

    @PostMapping("/{paymentId}/refunds")
    public CompletableFuture<PaymentResponse> refundPayment(@PathVariable String paymentId, @RequestParam BigDecimal amount) {
        return paymentService.refundPayment(paymentId, amount);
    }

    @GetMapping("/{paymentId}")
    public CompletableFuture<PaymentResponse> getPaymentStatus(@PathVariable String paymentId) {
        return paymentService.getPaymentStatus(paymentId);
    }
}
