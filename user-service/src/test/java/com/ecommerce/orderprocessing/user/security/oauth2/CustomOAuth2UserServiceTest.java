package com.ecommerce.orderprocessing.user.security.oauth2;

import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.user.domain.entity.Role;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import com.ecommerce.orderprocessing.user.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2User oAuth2User;
    private OAuth2UserRequest userRequest;
    private User existingUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = new Role("CUSTOMER");
        customerRole.setId(1L);

        existingUser = new User("John", "Doe", "john.doe@example.com", "password");
        existingUser.setId(1L);
        existingUser.setRoles(Set.of(customerRole));

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "john.doe@example.com");
        attributes.put("given_name", "John");
        attributes.put("family_name", "Doe");
        attributes.put("picture", "http://example.com/pic.jpg");

        oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(oAuth2User.getAttribute("email")).thenReturn("john.doe@example.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("John");
        when(oAuth2User.getAttribute("family_name")).thenReturn("Doe");
        when(oAuth2User.getAttribute("picture")).thenReturn("http://example.com/pic.jpg");

        userRequest = mock(OAuth2UserRequest.class);
        when(userRequest.getAccessToken()).thenReturn(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", Instant.now(), Instant.now().plusSeconds(3600)));
    }

    @Test
    void loadUser_shouldReturnExistingUser() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(existingUser));

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        assertNotNull(result);
        assertEquals(existingUser.getEmail(), result.getAttribute("email"));
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUser_shouldCreateNewUser() {
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User newUser = invocation.getArgument(0);
            newUser.setId(2L); // Simulate ID generation
            newUser.setRoles(Set.of(customerRole)); // Assign default role
            return newUser;
        });
        when(roleRepository.findByName("CUSTOMER")).thenReturn(Optional.of(customerRole));

        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        assertNotNull(result);
        assertEquals("john.doe@example.com", result.getAttribute("email"));
        verify(userRepository, times(1)).findByEmail("john.doe@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(roleRepository, times(1)).findByName("CUSTOMER");
    }
}