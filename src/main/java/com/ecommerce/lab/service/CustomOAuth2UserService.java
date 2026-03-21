package com.ecommerce.lab.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ecommerce.lab.filter.JwtUtils;
import com.ecommerce.lab.model.AuthProvider;
import com.ecommerce.lab.model.Role;
import com.ecommerce.lab.model.User;
import com.ecommerce.lab.repository.base.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String sub = oAuth2User.getAttribute("sub");

        // THIS IS WHERE YOUR LOGS WILL HIT
        System.out.println("Processing OAuth2 Login for: " + email);
        String token = jwtUtils.generateToken(email, false);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            System.out.println("Registering new Google User: " + email);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setProviderId(sub);
            newUser.setName(oAuth2User.getAttribute("name"));
            newUser.setUserName(name + "_google");
            newUser.setRole(Role.ROLE_USER);
            newUser.setProvider(AuthProvider.GOOGLE);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setToken(token);
            return userRepository.save(newUser);
        });

        // Update profile info every login to refresh
        user.setName(name);
        user.setProfilePicture(picture);
        user.setLastLogin(LocalDateTime.now());
        user.setToken(token);
        userRepository.save(user);

        return oAuth2User;
    }
}