package com.ecommerce.orderprocessing.user.controller;

import com.ecommerce.orderprocessing.user.dto.UserRegistrationRequest;
import com.ecommerce.orderprocessing.user.dto.LoginRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.dto.LoginResponse;
import com.ecommerce.orderprocessing.user.service.AuthenticationService;
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
    public CompletableFuture<ResponseEntity<UserResponse>> register(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        return authenticationService.register(registrationRequest)
                .thenApply(customer -> ResponseEntity.status(HttpStatus.CREATED).body(customer));
    }
}
