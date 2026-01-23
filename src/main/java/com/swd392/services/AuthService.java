package com.swd392.services;

import com.swd392.dtos.AuthenticationResponse;
import com.swd392.dtos.LoginRequest;
import com.swd392.dtos.UserInfoDto;
import com.swd392.entities.User;
import com.swd392.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticate user with email and password
     */
    public AuthenticationResponse authenticateWithEmailPassword(LoginRequest loginRequest) {
        try {
            // First check if user exists
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));

            // Check if user is active
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new RuntimeException("User account is not active. Status: " + user.getStatus());
            }

            // Check if user has a password (not OAuth-only user)
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new RuntimeException("User registered via OAuth. Please login with Google.");
            }

            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtTokenProvider.generateTokenForUser(user.getEmail());

            // Create user info DTO
            UserInfoDto userInfo = UserInfoDto.fromUser(user);

            return new AuthenticationResponse(jwt, userInfo);

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new RuntimeException("User account is disabled");
        } catch (org.springframework.security.authentication.LockedException e) {
            throw new RuntimeException("User account is locked");
        }
    }

    /**
     * Authenticate user with Google OAuth2
     */
    public AuthenticationResponse authenticateWithGoogle(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");
        String avatarUrl = (String) attributes.get("picture");

        // Find or create user
        User user = findOrCreateGoogleUser(email, name, providerId, avatarUrl);

        // Generate JWT token
        String jwt = jwtTokenProvider.generateTokenForUser(user.getEmail());

        // Create user info DTO
        UserInfoDto userInfo = UserInfoDto.fromUser(user);

        return new AuthenticationResponse(jwt, userInfo);
    }

    /**
     * Find existing Google user or create a new one
     */
    private User findOrCreateGoogleUser(String email, String name, String providerId, String avatarUrl) {
        // First, try to find user by email and provider
        Optional<User> existingUser = userRepository.findByEmailAndProvider(email, "google");

        if (existingUser.isPresent()) {
            // Update user info if needed
            User user = existingUser.get();
            user.setFullName(name);
            user.setAvatarUrl(avatarUrl);
            user.setProviderId(providerId);
            return userRepository.save(user);
        }

        // Check if user exists with same email but different provider
        Optional<User> userWithSameEmail = userRepository.findByEmail(email);
        if (userWithSameEmail.isPresent()) {
            // Link Google account to existing user
            User user = userWithSameEmail.get();
            user.setProvider("google");
            user.setProviderId(providerId);
            user.setAvatarUrl(avatarUrl);
            return userRepository.save(user);
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(name);
        newUser.setProvider("google");
        newUser.setProviderId(providerId);
        newUser.setAvatarUrl(avatarUrl);
        newUser.setRole(User.UserRole.STUDENT); // Default role for new OAuth users
        newUser.setStatus(User.UserStatus.ACTIVE);

        return userRepository.save(newUser);
    }

    /**
     * Get current authenticated user
     */
    public UserInfoDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserInfoDto.fromUser(user);
    }

    /**
     * Create Spring Security Authentication object from User
     */
    public Authentication createAuthentication(User user) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                authorities
        );
    }
}
