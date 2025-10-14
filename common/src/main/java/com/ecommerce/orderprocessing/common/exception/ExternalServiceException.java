package com.ecommerce.orderprocessing.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error communicating with external service: %s. Reason: %s", serviceName, message));
    }
}
