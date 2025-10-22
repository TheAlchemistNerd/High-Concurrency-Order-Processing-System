package com.ecommerce.orderprocessing.user.security.oauth2;

import com.ecommerce.orderprocessing.user.domain.entity.User;
import com.ecommerce.orderprocessing.user.domain.entity.Role;
import com.ecommerce.orderprocessing.user.repository.UserRepository;
import com.ecommerce.orderprocessing.user.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update user attributes if necessary
        } else {
            // Create a new user
            user = new User();
            user.setEmail(email);
            user.setFirstName(oAuth2User.getAttribute("given_name"));
            user.setLastName(oAuth2User.getAttribute("family_name"));
            user.setProfilePictureUrl(oAuth2User.getAttribute("picture"));
            user.setIsActive(true);
            userRepository.save(user);
        }

        String roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("id", user.getId());
        attributes.put("email", user.getEmail());

        return new AppUserDetails(user.getId(), user.getEmail(), user.getPasswordHash(), roles, user.getIsActive(), attributes);
    }
}
