package com.ecommerce.orderprocessing.user.service;

import com.ecommerce.orderprocessing.common.exception.ResourceNotFoundException;
import com.ecommerce.orderprocessing.user.domain.entity.Address;
import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.user.dto.AddressRequest;
import com.ecommerce.orderprocessing.user.dto.AddressResponse;
import com.ecommerce.orderprocessing.user.dto.ChangePasswordRequest;
import com.ecommerce.orderprocessing.user.dto.UserProfileUpdateRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.repository.AddressRepository;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private PasswordEncoderService passwordEncoderService;

    private ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Address testAddress;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, addressRepository, passwordEncoderService, virtualThreadExecutor);

        testUser = new User("John", "Doe", "john.doe@example.com", "hashedPassword");
        testUser.setId(1L);
        testUser.setPhoneNumber("1234567890");
        testUser.setProfilePictureUrl("http://example.com/pic.jpg");
        testUser.setCreatedAt(LocalDateTime.now());

        testAddress = new Address();
        testAddress.setId(10L);
        testAddress.setUser(testUser);
        testAddress.setStreet("123 Main St");
        testAddress.setCity("Anytown");
        testAddress.setState("CA");
        testAddress.setPostalCode("90210");
        testAddress.setCountry("USA");
        testAddress.setIsDefaultShipping(true);
        testAddress.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getUserProfile_shouldReturnUserResponse() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        CompletableFuture<UserResponse> future = userService.getUserProfile(1L);
        UserResponse response = future.get();

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(testUser.getEmail());
        assertThat(response.firstName()).isEqualTo(testUser.getFirstName());
    }

    @Test
    void getUserProfile_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<UserResponse> future = userService.getUserProfile(1L);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void addAddress_shouldReturnAddressResponse() throws Exception {
        AddressRequest request = new AddressRequest("456 Oak Ave", "Otherville", "NY", "10001", "USA", false, false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

        CompletableFuture<AddressResponse> future = userService.addAddress(1L, request);
        AddressResponse response = future.get();

        assertThat(response).isNotNull();
        assertThat(response.street()).isEqualTo(testAddress.getStreet());
    }

    @Test
    void addAddress_whenUserNotFound_shouldThrowException() {
        AddressRequest request = new AddressRequest("456 Oak Ave", "Otherville", "NY", "10001", "USA", false, false);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<AddressResponse> future = userService.addAddress(1L, request);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAddresses_shouldReturnListOfAddressResponses() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserId(1L)).thenReturn(Arrays.asList(testAddress));

        CompletableFuture<List<AddressResponse>> future = userService.getAddresses(1L);
        List<AddressResponse> responses = future.get();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).street()).isEqualTo(testAddress.getStreet());
    }

    @Test
    void getAddresses_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<List<AddressResponse>> future = userService.getAddresses(1L);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAddress_shouldReturnUpdatedAddressResponse() throws Exception {
        AddressRequest request = new AddressRequest("789 Pine St", "Newtown", "TX", "77001", "USA", false, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<AddressResponse> future = userService.updateAddress(1L, 10L, request);
        AddressResponse response = future.get();

        assertThat(response).isNotNull();
        assertThat(response.street()).isEqualTo(request.street());
        assertThat(response.isDefaultBilling()).isTrue();
    }

    @Test
    void updateAddress_whenAddressNotFound_shouldThrowException() {
        AddressRequest request = new AddressRequest("789 Pine St", "Newtown", "TX", "77001", "USA", false, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<AddressResponse> future = userService.updateAddress(1L, 10L, request);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateAddress_whenUserNotFound_shouldThrowException() {
        AddressRequest request = new AddressRequest("789 Pine St", "Newtown", "TX", "77001", "USA", false, true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<AddressResponse> future = userService.updateAddress(1L, 10L, request);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAddress_shouldCompleteSuccessfully() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(testAddress));
        doNothing().when(addressRepository).delete(any(Address.class));

        CompletableFuture<Void> future = userService.deleteAddress(1L, 10L);
        future.get();

        verify(addressRepository, times(1)).delete(testAddress);
    }

    @Test
    void deleteAddress_whenAddressNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<Void> future = userService.deleteAddress(1L, 10L);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteAddress_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<Void> future = userService.deleteAddress(1L, 10L);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUserProfile_shouldReturnUpdatedUserResponse() throws Exception {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("Jane", "Doe", "0987654321", "http://example.com/newpic.jpg");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<UserResponse> future = userService.updateUserProfile(1L, request);
        UserResponse response = future.get();

        assertThat(response).isNotNull();
        assertThat(response.firstName()).isEqualTo(request.firstName());
        assertThat(response.profilePictureUrl()).isEqualTo(request.profilePictureUrl());
    }

    @Test
    void updateUserProfile_whenUserNotFound_shouldThrowException() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("Jane", "Doe", "0987654321", "http://example.com/newpic.jpg");
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<UserResponse> future = userService.updateUserProfile(1L, request);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changePassword_shouldCompleteSuccessfully() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoderService.verifyPassword("oldPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoderService.encodePassword("newPassword123")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<Void> future = userService.changePassword(1L, request);
        future.get();

        verify(userRepository, times(1)).save(testUser);
        assertThat(testUser.getPasswordHash()).isEqualTo("newHashedPassword");
    }

    @Test
    void changePassword_whenUserNotFound_shouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<Void> future = userService.changePassword(1L, request);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void changePassword_whenCurrentPasswordInvalid_shouldThrowException() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword123");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoderService.verifyPassword("wrongPassword", "hashedPassword")).thenReturn(false);

        CompletableFuture<Void> future = userService.changePassword(1L, request);

        assertThatThrownBy(future::get).hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteUserAccount_shouldCompleteSuccessfully() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        CompletableFuture<Void> future = userService.deleteUserAccount(1L);
        future.get();

        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteUserAccount_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        CompletableFuture<Void> future = userService.deleteUserAccount(1L);

        assertThatThrownBy(future::get).hasCauseInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllUsers_shouldReturnListOfUserResponses() throws Exception {
        User user2 = new User("Jane", "Smith", "jane.smith@example.com", "hashedPassword2");
        user2.setId(2L);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        CompletableFuture<List<UserResponse>> future = userService.getAllUsers();
        List<UserResponse> responses = future.get();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).email()).isEqualTo(testUser.getEmail());
        assertThat(responses.get(1).email()).isEqualTo(user2.getEmail());
    }
}
