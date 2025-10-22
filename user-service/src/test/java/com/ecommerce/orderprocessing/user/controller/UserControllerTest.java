package com.ecommerce.orderprocessing.user.controller;

import com.ecommerce.orderprocessing.user.dto.AddressRequest;
import com.ecommerce.orderprocessing.user.dto.AddressResponse;
import com.ecommerce.orderprocessing.user.dto.ChangePasswordRequest;
import com.ecommerce.orderprocessing.user.dto.UserProfileUpdateRequest;
import com.ecommerce.orderprocessing.user.dto.UserResponse;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import com.ecommerce.orderprocessing.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserResponse userResponse;
    private AddressResponse addressResponse;
    private AppUserDetails appUserDetails;

    @BeforeEach
    void setUp() {
        userResponse = new UserResponse(
                1L, "John", "Doe", "john.doe@example.com", "123-456-7890",
                "http://example.com/pic.jpg", "CUSTOMER", true, LocalDateTime.now()
        );

        addressResponse = new AddressResponse(
                10L, "123 Main St", "Anytown", "CA", "12345", "USA", true, false, LocalDateTime.now()
        );

        appUserDetails = new AppUserDetails(1L, "john.doe@example.com", "password", "CUSTOMER", true, Collections.emptyMap());

        // Set up security context for authenticated user
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                appUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void getMyProfile_shouldReturnUserProfile() throws Exception {
        when(userService.getUserProfile(anyLong())).thenReturn(CompletableFuture.completedFuture(userResponse));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(userResponse.id()))
                .andExpect(jsonPath(".email").value(userResponse.email()));
    }

    @Test
    void updateMyProfile_shouldReturnUpdatedUserProfile() throws Exception {
        UserProfileUpdateRequest updateRequest = new UserProfileUpdateRequest("Jane", "Smith", "987-654-3210", "http://new.pic.url");
        UserResponse updatedUserResponse = new UserResponse(
                1L, "Jane", "Smith", "john.doe@example.com", "987-654-3210",
                "http://new.pic.url", "CUSTOMER", true, LocalDateTime.now()
        );

        when(userService.updateUserProfile(anyLong(), any(UserProfileUpdateRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(updatedUserResponse));

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".firstName").value(updatedUserResponse.firstName()))
                .andExpect(jsonPath(".lastName").value(updatedUserResponse.lastName()));
    }

    @Test
    void addMyAddress_shouldReturnCreatedAddress() throws Exception {
        AddressRequest addressRequest = new AddressRequest("456 Oak Ave", "Othertown", "NY", "67890", "USA", true, false);
        when(userService.addAddress(anyLong(), any(AddressRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(addressResponse));

        mockMvc.perform(post("/api/users/me/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(".street").value(addressResponse.street()));
    }

    @Test
    void getMyAddresses_shouldReturnListOfAddresses() throws Exception {
        List<AddressResponse> addresses = Collections.singletonList(addressResponse);
        when(userService.getAddresses(anyLong())).thenReturn(CompletableFuture.completedFuture(addresses));

        mockMvc.perform(get("/api/users/me/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].street").value(addressResponse.street()));
    }

    @Test
    void updateMyAddress_shouldReturnUpdatedAddress() throws Exception {
        AddressRequest addressRequest = new AddressRequest("Updated St", "Updated City", "TX", "54321", "USA", false, true);
        AddressResponse updatedAddressResponse = new AddressResponse(
                10L, "Updated St", "Updated City", "TX", "54321", "USA", false, true, LocalDateTime.now()
        );
        when(userService.updateAddress(anyLong(), anyLong(), any(AddressRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(updatedAddressResponse));

        mockMvc.perform(put("/api/users/me/addresses/{addressId}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".street").value(updatedAddressResponse.street()));
    }

    @Test
    void deleteMyAddress_shouldReturnNoContent() throws Exception {
        when(userService.deleteAddress(anyLong(), anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/api/users/me/addresses/{addressId}", 10L))
                .andExpect(status().isNoContent());
    }

    @Test
    void changeMyPassword_shouldReturnNoContent() throws Exception {
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest("oldPass", "newPass");
        when(userService.changePassword(anyLong(), any(ChangePasswordRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMyAccount_shouldReturnNoContent() throws Exception {
        when(userService.deleteUserAccount(anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUserProfileById_adminAccess_shouldReturnUserProfile() throws Exception {
        // Simulate admin authentication
        AppUserDetails adminUserDetails = new AppUserDetails(2L, "admin@example.com", "password", "ADMIN", true, Collections.emptyMap());
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuthentication);

        when(userService.getUserProfile(anyLong())).thenReturn(CompletableFuture.completedFuture(userResponse));

        mockMvc.perform(get("/api/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(userResponse.id()));
    }

    @Test
    void getAllUsers_adminAccess_shouldReturnListOfUsers() throws Exception {
        // Simulate admin authentication
        AppUserDetails adminUserDetails = new AppUserDetails(2L, "admin@example.com", "password", "ADMIN", true, Collections.emptyMap());
        Authentication adminAuthentication = new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuthentication);

        List<UserResponse> allUsers = Arrays.asList(userResponse, new UserResponse(
                2L, "Admin", "User", "admin@example.com", "111-222-3333",
                "http://example.com/admin.jpg", "ADMIN", true, LocalDateTime.now()
        ));
        when(userService.getAllUsers()).thenReturn(CompletableFuture.completedFuture(allUsers));

        mockMvc.perform(get("/api/users/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }
}