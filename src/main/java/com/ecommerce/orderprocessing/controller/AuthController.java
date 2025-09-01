package com.ecommerce.orderprocessing.controller;

import com.ecommerce.orderprocessing.dto.request.CustomerRegistrationRequest;
import com.ecommerce.orderprocessing.dto.request.LoginRequest;
import com.ecommerce.orderprocessing.dto.response.CustomerResponse;
import com.ecommerce.orderprocessing.dto.response.LoginResponse;
import com.ecommerce.orderprocessing.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authenticationService.authenticate(loginRequest)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/register")
    public CompletableFuture<ResponseEntity<CustomerResponse>> register(@Valid @RequestBody CustomerRegistrationRequest registrationRequest) {
        return authenticationService.register(registrationRequest)
                .thenApply(customer -> ResponseEntity.status(HttpStatus.CREATED).body(customer));
    }
}
