package com.ecommerce.orderprocessing.user.controller;

import com.ecommerce.orderprocessing.user.dto.AddressRequest;
import com.ecommerce.orderprocessing.user.dto.AddressResponse;
import com.ecommerce.orderprocessing.user.dto.ChangePasswordRequest;
import com.ecommerce.orderprocessing.user.dto.UserProfileUpdateRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.service.UserService;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

        @GetMapping("/me")
        @PreAuthorize("hasRole('CUSTOMER')")
        public CompletableFuture<ResponseEntity<UserResponse>> getMyProfile(Authentication authentication) {
            Long userId = ((AppUserDetails) authentication.getPrincipal()).id();
            return userService.getUserProfile(userId)
                    .thenApply(ResponseEntity::ok);
        }
    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<ResponseEntity<UserResponse>> updateMyProfile(Authentication authentication, @Valid @RequestBody UserProfileUpdateRequest updateRequest) {
        Long userId = ((AppUserDetails) authentication.getPrincipal()).id();
        return userService.updateUserProfile(userId, updateRequest)
                .thenApply(ResponseEntity::ok);
    }
    @PostMapping("/me/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<ResponseEntity<AddressResponse>> addMyAddress(Authentication authentication, @Valid @RequestBody AddressRequest addressRequest) {
        Long userId = ((AppUserDetails) authentication.getPrincipal()).id();
        return userService.addAddress(userId, addressRequest)
                .thenApply(address -> new ResponseEntity<>(address, HttpStatus.CREATED));
    }
    @GetMapping("/me/addresses")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<ResponseEntity<List<AddressResponse>>> getMyAddresses(Authentication authentication) {
        Long userId = ((AppUserDetails) authentication.getPrincipal()).id();
        return userService.getAddresses(userId)
                .thenApply(ResponseEntity::ok);
    }

    @PutMapping("/me/addresses/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<ResponseEntity<AddressResponse>> updateMyAddress(Authentication authentication, @PathVariable Long addressId, @Valid @RequestBody AddressRequest addressRequest) {
        Long userId = ((AppUserDetails) authentication.getPrincipal()).id();
        return userService.updateAddress(userId, addressId, addressRequest)
                .thenApply(ResponseEntity::ok);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<ResponseEntity<Void>> deleteMyAddress(Authentication authentication, @PathVariable Long addressId) {
        Long userId = ((AppUserDetails) authentication.getPrincipal()).id();
        return userService.deleteAddress(userId, addressId)
                .thenApply(__ -> ResponseEntity.noContent().build());
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<ResponseEntity<Void>> changeMyPassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        Long userId = ((AppUserDetails) authentication.getPrincipal()).id();
        return userService.changePassword(userId, changePasswordRequest)
                .thenApply(__ -> ResponseEntity.noContent().build());
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CompletableFuture<ResponseEntity<Void>> deleteMyAccount(Authentication authentication) {
        Long userId = ((AppUserDetails) authentication.getPrincipal()).id();
        return userService.deleteUserAccount(userId)
                .thenApply(__ -> ResponseEntity.noContent().build());
    }

    // Admin/Support endpoints
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    public CompletableFuture<ResponseEntity<UserResponse>> getUserProfileById(@PathVariable Long userId) {
        return userService.getUserProfile(userId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<List<UserResponse>>> getAllUsers() {
        return userService.getAllUsers()
                .thenApply(ResponseEntity::ok);
    }
}