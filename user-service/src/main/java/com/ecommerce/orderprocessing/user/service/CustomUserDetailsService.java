package com.ecommerce.orderprocessing.user.service;

import com.ecommerce.orderprocessing.user.repository.UserRepository;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import com.ecommerce.orderprocessing.user.domain.entity.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        return userRepository.findByEmail(email)
                .map(user -> new AppUserDetails(
                        user.getId(),
                        user.getEmail(),
                        user.getPasswordHash(),
                        user.getRoles().stream().map(Role::getName).collect(Collectors.joining(",")),
                        user.getIsActive() != null ? user.getIsActive() : false,
                        new java.util.HashMap<>()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
