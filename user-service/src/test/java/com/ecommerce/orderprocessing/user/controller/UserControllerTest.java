package com.ecommerce.orderprocessing.user.controller;

import com.ecommerce.orderprocessing.user.dto.AddressRequest;
import com.ecommerce.orderprocessing.user.dto.AddressResponse;
import com.ecommerce.orderprocessing.user.dto.ChangePasswordRequest;
import com.ecommerce.orderprocessing.user.dto.UserProfileUpdateRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.service.UserService;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private Authentication customerAuthentication;
    private AppUserDetails customerAppUserDetails;
    private UserResponse customerUserResponse;
    private AddressResponse addressResponse;

    @BeforeEach
    void setUp() {
        customerAppUserDetails = new AppUserDetails(1L, "test@example.com", "password", "ROLE_CUSTOMER", true);
        customerAuthentication = new UsernamePasswordAuthenticationToken(customerAppUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        customerUserResponse = new UserResponse(
                1L, "John", "Doe", "john.doe@example.com", "1234567890", "http://pic.url", "ROLE_CUSTOMER", true, LocalDateTime.now()
        );

        addressResponse = new AddressResponse(
                10L, "123 Main St", "Anytown", "CA", "90210", "USA", true, false, LocalDateTime.now()
        );
    }

    @Test
    void getMyProfile_shouldReturnUserProfile() throws Exception {
        when(userService.getUserProfile(anyLong())).thenReturn(CompletableFuture.completedFuture(customerUserResponse));

        mockMvc.perform(get("/api/users/me")
                        .with(authentication(customerAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(customerUserResponse.email()))
                .andExpect(jsonPath("$.firstName").value(customerUserResponse.firstName()));
    }

    @Test
    void updateMyProfile_shouldReturnUpdatedUserProfile() throws Exception {
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest("Jane", "Smith", "0987654321", "http://new.pic");
        UserResponse updatedUserResponse = new UserResponse(
                1L, "Jane", "Smith", "john.doe@example.com", "0987654321", "http://new.pic", "ROLE_CUSTOMER", true, LocalDateTime.now()
        );
        when(userService.updateUserProfile(anyLong(), any(UserProfileUpdateRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(updatedUserResponse));

        mockMvc.perform(put("/api/users/me")
                        .with(authentication(customerAuthentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Jane\", \"lastName\":\"Smith\", \"phoneNumber\":\"0987654321\", \"profilePictureUrl\":\"http://new.pic\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.phoneNumber").value("0987654321"));
    }

    @Test
    void addMyAddress_shouldReturnNewAddress() throws Exception {
        AddressRequest addressRequest = new AddressRequest("456 Oak Ave", "Otherville", "NY", "10001", "USA", false, false);
        when(userService.addAddress(anyLong(), any(AddressRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(addressResponse));

        mockMvc.perform(post("/api/users/me/addresses")
                        .with(authentication(customerAuthentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"street\":\"456 Oak Ave\", \"city\":\"Otherville\", \"state\":\"NY\", \"postalCode\":\"10001\", \"country\":\"USA\", \"isDefaultShipping\":false, \"isDefaultBilling\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.street").value(addressResponse.street()));
    }

    @Test
    void getMyAddresses_shouldReturnListOfAddresses() throws Exception {
        List<AddressResponse> addresses = Arrays.asList(addressResponse);
        when(userService.getAddresses(anyLong())).thenReturn(CompletableFuture.completedFuture(addresses));

        mockMvc.perform(get("/api/users/me/addresses")
                        .with(authentication(customerAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].street").value(addressResponse.street()));
    }

    @Test
    void updateMyAddress_shouldReturnUpdatedAddress() throws Exception {
        AddressRequest addressRequest = new AddressRequest("789 Pine St", "Newtown", "TX", "77001", "USA", false, true);
        AddressResponse updatedAddressResponse = new AddressResponse(
                10L, "789 Pine St", "Newtown", "TX", "77001", "USA", false, true, LocalDateTime.now()
        );
        when(userService.updateAddress(anyLong(), anyLong(), any(AddressRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(updatedAddressResponse));

        mockMvc.perform(put("/api/users/me/addresses/{addressId}", 10L)
                        .with(authentication(customerAuthentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"street\":\"789 Pine St\", \"city\":\"Newtown\", \"state\":\"TX\", \"postalCode\":\"77001\", \"country\":\"USA\", \"isDefaultShipping\":false, \"isDefaultBilling\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value(updatedAddressResponse.street()));
    }

    @Test
    void deleteMyAddress_shouldReturnNoContent() throws Exception {
        when(userService.deleteAddress(anyLong(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/api/users/me/addresses/{addressId}", 10L)
                        .with(authentication(customerAuthentication)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changeMyPassword_shouldReturnNoContent() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("oldPass", "newPass123");
        when(userService.changePassword(anyLong(), any(ChangePasswordRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/users/me/change-password")
                        .with(authentication(customerAuthentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"oldPass\", \"newPassword\":\"newPass123\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMyAccount_shouldReturnNoContent() throws Exception {
        when(userService.deleteUserAccount(anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/api/users/me")
                        .with(authentication(customerAuthentication)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUserProfileById_asAdmin_shouldReturnUserProfile() throws Exception {
        AppUserDetails adminAppUserDetails = new AppUserDetails(2L, "admin@example.com", "password", "ROLE_ADMIN", true);
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(adminAppUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(userService.getUserProfile(anyLong())).thenReturn(CompletableFuture.completedFuture(customerUserResponse));

        mockMvc.perform(get("/api/users/{userId}", 1L)
                        .with(authentication(adminAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(customerUserResponse.email()));
    }

    @Test
    void getAllUsers_asAdmin_shouldReturnListOfUsers() throws Exception {
        AppUserDetails adminAppUserDetails = new AppUserDetails(2L, "admin@example.com", "password", "ROLE_ADMIN", true);
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(adminAppUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        UserResponse user2 = new UserResponse(
                2L, "Jane", "Smith", "jane.smith@example.com", "0987654321", "http://pic2.url", "ROLE_CUSTOMER", true, LocalDateTime.now()
        );
        List<UserResponse> allUsers = Arrays.asList(customerUserResponse, user2);
        when(userService.getAllUsers()).thenReturn(CompletableFuture.completedFuture(allUsers));

        mockMvc.perform(get("/api/users/")
                        .with(authentication(adminAuthentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(customerUserResponse.email()))
                .andExpect(jsonPath("$[1].email").value(user2.email()));
    }
}