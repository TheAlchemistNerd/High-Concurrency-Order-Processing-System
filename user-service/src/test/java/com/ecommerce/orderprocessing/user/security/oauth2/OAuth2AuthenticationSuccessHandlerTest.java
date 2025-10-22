package com.ecommerce.orderprocessing.user.security.oauth2;

import com.ecommerce.orderprocessing.user.security.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    private String testEmail = "test@example.com";
    private String testRole = "ROLE_CUSTOMER";
    private String testToken = "mocked_jwt_token";

    @BeforeEach
    void setUp() {
        when(authentication.getName()).thenReturn(testEmail);
        when(authentication.getAuthorities()).thenReturn(Collections.singletonList(new SimpleGrantedAuthority(testRole)));
        when(jwtTokenProvider.generateToken(testEmail, testRole)).thenReturn(testToken);

        // Inject mock RedirectStrategy
        oAuth2AuthenticationSuccessHandler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    void onAuthenticationSuccess_shouldGenerateTokenAndRedirect() throws ServletException, IOException {
        oAuth2AuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        String expectedRedirectUrl = UriComponentsBuilder.fromUriString("/login/oauth2/code/home")
                .queryParam("token", testToken)
                .build().toUriString();

        verify(jwtTokenProvider, times(1)).generateToken(testEmail, testRole);
        verify(redirectStrategy, times(1)).sendRedirect(request, response, expectedRedirectUrl);
    }
}