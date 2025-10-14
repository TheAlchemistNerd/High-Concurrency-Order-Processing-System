package com.ecommerce.orderprocessing.user.service;

import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.user.domain.entity.Address;
import com.ecommerce.orderprocessing.user.dto.AddressRequest;
import com.ecommerce.orderprocessing.user.dto.AddressResponse;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.domain.entity.Role;
import com.ecommerce.orderprocessing.user.dto.ChangePasswordRequest;
import com.ecommerce.orderprocessing.user.dto.UserProfileUpdateRequest;
import com.ecommerce.orderprocessing.user.repository.AddressRepository;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoderService passwordEncoderService;
    private final ExecutorService virtualThreadExecutor;

    public UserService(UserRepository userRepository, AddressRepository addressRepository, PasswordEncoderService passwordEncoderService, ExecutorService virtualThreadExecutor) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.passwordEncoderService = passwordEncoderService;
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public CompletableFuture<UserResponse> getUserProfile(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return convertToUserResponse(user);
        }, virtualThreadExecutor);
    }

    @Transactional
    public CompletableFuture<AddressResponse> addAddress(Long userId, AddressRequest addressRequest) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Address address = new Address();
            address.setUser(user);
            address.setStreet(addressRequest.street());
            address.setCity(addressRequest.city());
            address.setState(addressRequest.state());
            address.setPostalCode(addressRequest.postalCode());
            address.setCountry(addressRequest.country());
            address.setIsDefaultShipping(addressRequest.isDefaultShipping() != null ? addressRequest.isDefaultShipping() : false);
            address.setIsDefaultBilling(addressRequest.isDefaultBilling() != null ? addressRequest.isDefaultBilling() : false);

            Address savedAddress = addressRepository.save(address);
            return convertToAddressResponse(savedAddress);
        }, virtualThreadExecutor);
    }

    @Transactional
    public CompletableFuture<AddressResponse> updateAddress(Long userId, Long addressId, AddressRequest addressRequest) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

            if (!address.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Address not found for this user");
            }

            address.setStreet(addressRequest.street());
            address.setCity(addressRequest.city());
            address.setState(addressRequest.state());
            address.setPostalCode(addressRequest.postalCode());
            address.setCountry(addressRequest.country());
            address.setIsDefaultShipping(addressRequest.isDefaultShipping() != null ? addressRequest.isDefaultShipping() : false);
            address.setIsDefaultBilling(addressRequest.isDefaultBilling() != null ? addressRequest.isDefaultBilling() : false);

            Address updatedAddress = addressRepository.save(address);
            return convertToAddressResponse(updatedAddress);
        }, virtualThreadExecutor);
    }

    public CompletableFuture<List<AddressResponse>> getAddresses(Long userId) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            List<Address> addresses = addressRepository.findByUserId(userId);
            return addresses.stream().map(this::convertToAddressResponse).collect(Collectors.toList());
        }, virtualThreadExecutor);
    }

    @Transactional
    public CompletableFuture<Void> deleteUserAccount(Long userId) {
        return CompletableFuture.runAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            userRepository.delete(user);
        }, virtualThreadExecutor);
    }

    @Transactional
    public CompletableFuture<Void> changePassword(Long userId, ChangePasswordRequest changePasswordRequest) {
        return CompletableFuture.runAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!passwordEncoderService.verifyPassword(changePasswordRequest.currentPassword(), user.getPasswordHash())) {
                throw new IllegalArgumentException("Current password does not match");
            }

            user.setPasswordHash(passwordEncoderService.encodePassword(changePasswordRequest.newPassword()));
            userRepository.save(user);
        }, virtualThreadExecutor);
    }

    @Transactional
    public CompletableFuture<UserResponse> updateUserProfile(Long userId, UserProfileUpdateRequest updateRequest) {
        return CompletableFuture.supplyAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (updateRequest.firstName() != null) {
                user.setFirstName(updateRequest.firstName());
            }
            if (updateRequest.lastName() != null) {
                user.setLastName(updateRequest.lastName());
            }
            if (updateRequest.phoneNumber() != null) {
                user.setPhoneNumber(updateRequest.phoneNumber());
            }
            if (updateRequest.profilePictureUrl() != null) {
                user.setProfilePictureUrl(updateRequest.profilePictureUrl());
            }

            User updatedUser = userRepository.save(user);
            return convertToUserResponse(updatedUser);
        }, virtualThreadExecutor);
    }

    @Transactional
    public CompletableFuture<Void> deleteAddress(Long userId, Long addressId) {
        return CompletableFuture.runAsync(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            Address address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

            if (!address.getUser().getId().equals(userId)) {
                throw new ResourceNotFoundException("Address not found for this user");
            }
            addressRepository.delete(address);
        }, virtualThreadExecutor);
    }

    public CompletableFuture<List<UserResponse>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            List<User> users = userRepository.findAll();
            return users.stream().map(this::convertToUserResponse).collect(Collectors.toList());
        }, virtualThreadExecutor);
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getProfilePictureUrl(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }

    private AddressResponse convertToAddressResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getIsDefaultShipping(),
                address.getIsDefaultBilling(),
                address.getCreatedAt()
        );
    }
}
