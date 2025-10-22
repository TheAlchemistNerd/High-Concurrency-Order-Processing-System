package com.ecommerce.orderprocessing.user.service;

import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.user.domain.entity.Address;
import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.user.dto.AddressRequest;
import com.ecommerce.orderprocessing.user.dto.AddressResponse;
import com.ecommerce.orderprocessing.user.dto.ChangePasswordRequest;
import com.ecommerce.orderprocessing.user.dto.UserProfileUpdateRequest;
import com.ecommerce.orderprocessing.user.repository.AddressRepository;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PasswordEncoderService passwordEncoderService;

    // Use a real ExecutorService for CompletableFuture testing
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @InjectMocks
    private UserService userService;

    private User user;
    private Address address;

    @BeforeEach
    void setUp() {
        // Manually inject the real ExecutorService into the service under test
        userService = new UserService(userRepository, addressRepository, passwordEncoderService, virtualThreadExecutor);

        user = new User("John", "Doe", "john.doe@example.com", "encodedPassword");
        user.setId(1L);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());

        address = new Address();
        address.setId(10L);
        address.setUser(user);
        address.setStreet("123 Main St");
        address.setCity("Anytown");
        address.setState("CA");
        address.setPostalCode("12345");
        address.setCountry("USA");
        address.setIsDefaultShipping(true);
        address.setIsDefaultBilling(false);
        address.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getUserProfile_shouldReturnUserProfile() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var response = userService.getUserProfile(1L).get();

        assertNotNull(response);
        assertEquals(user.getId(), response.id());
        assertEquals(user.getEmail(), response.email());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserProfile_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserProfile(99L).join());
        verify(userRepository, times(1)).findById(99L);
    }

    @Test
    void addAddress_shouldReturnAddressResponse() throws Exception {
        AddressRequest request = new AddressRequest("456 Oak Ave", "Otherville", "NY", "67890", "USA", true, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address savedAddress = invocation.getArgument(0);
            savedAddress.setId(11L);
            return savedAddress;
        });

        var response = userService.addAddress(1L, request).get();

        assertNotNull(response);
        assertEquals(request.street(), response.street());
        verify(userRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void updateAddress_shouldReturnUpdatedAddressResponse() throws Exception {
        AddressRequest request = new AddressRequest("Updated St", "Updated City", "TX", "54321", "USA", false, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.updateAddress(1L, 10L, request).get();

        assertNotNull(response);
        assertEquals(request.street(), response.street());
        verify(userRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).findById(10L);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void getAddresses_shouldReturnListOfAddressResponses() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserId(1L)).thenReturn(Collections.singletonList(address));

        var responses = userService.getAddresses(1L).get();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(address.getStreet(), responses.get(0).street());
        verify(userRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).findByUserId(1L);
    }

    @Test
    void deleteUserAccount_shouldDeleteUser() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(any(User.class));

        userService.deleteUserAccount(1L).get();

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void changePassword_shouldUpdatePassword() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoderService.verifyPassword("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoderService.encodePassword("newPassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.changePassword(1L, request).get();

        assertEquals("newEncodedPassword", user.getPasswordHash());
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoderService, times(1)).verifyPassword("oldPassword", "encodedPassword");
        verify(passwordEncoderService, times(1)).encodePassword("newPassword");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserProfile_shouldUpdateFields() throws Exception {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("Jane", "Smith", "987-654-3210", "http://new.pic.url");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = userService.updateUserProfile(1L, request).get();

        assertNotNull(response);
        assertEquals(request.firstName(), response.firstName());
        assertEquals(request.lastName(), response.lastName());
        assertEquals(request.phoneNumber(), response.phoneNumber());
        assertEquals(request.profilePictureUrl(), response.profilePictureUrl());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteAddress_shouldDeleteAddress() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));
        doNothing().when(addressRepository).delete(any(Address.class));

        userService.deleteAddress(1L, 10L).get();

        verify(userRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).findById(10L);
        verify(addressRepository, times(1)).delete(address);
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        User user2 = new User("Jane", "Smith", "jane.smith@example.com", "anotherEncodedPassword");
        user2.setId(2L);
        user2.setIsActive(true);
        user2.setCreatedAt(LocalDateTime.now());

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        var responses = userService.getAllUsers().get();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(user.getEmail(), responses.get(0).email());
        assertEquals(user2.getEmail(), responses.get(1).email());
        verify(userRepository, times(1)).findAll();
    }
}