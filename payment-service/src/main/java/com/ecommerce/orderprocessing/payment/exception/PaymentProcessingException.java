package com.ecommerce.orderprocessing.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PaymentProcessingException extends RuntimeException {

    public PaymentProcessingException(String message) {
        super(message);
    }
}
