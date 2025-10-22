package com.ecommerce.orderprocessing.user.controller;

import com.ecommerce.orderprocessing.user.dto.UserRegistrationRequest;
import com.ecommerce.orderprocessing.user.dto.LoginRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.dto.LoginResponse;
import com.ecommerce.orderprocessing.user.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.concurrent.CompletableFuture;

@Controller
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

    @GetMapping("/login/oauth2")
    public RedirectView oauth2Login(@RequestParam("provider") String provider) {
        return new RedirectView("/oauth2/authorization/" + provider);
    }

    @GetMapping("/login/oauth2/code/home")
    public RedirectView oauth2Home(@RequestParam("token") String token) {
        return new RedirectView("http://localhost:3000/oauth2/redirect?token=" + token);
    }
}
