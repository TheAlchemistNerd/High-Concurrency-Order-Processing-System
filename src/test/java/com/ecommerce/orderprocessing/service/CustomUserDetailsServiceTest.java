package com.ecommerce.orderprocessing.service;

import com.ecommerce.orderprocessing.domain.entity.Customer;
import com.ecommerce.orderprocessing.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(customerRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        // Given
        Customer customer = new Customer("Test Customer", "test@test.com", "password");
        when(customerRepository.findByEmail("test@test.com")).thenReturn(Optional.of(customer));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrowException() {
        // Given
        when(customerRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("test@test.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
