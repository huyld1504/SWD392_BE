package com.swd392.services;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.authDTO.AuthenticationResponse;
import com.swd392.dtos.authDTO.LoginRequest;
import com.swd392.dtos.authDTO.RegisterRequest;
import com.swd392.dtos.userDTO.UserInfoDto;
import com.swd392.entities.User;
import com.swd392.exceptions.AppException;
import com.swd392.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Authenticate user with email and password
     */
    public AuthenticationResponse authenticateWithEmailPassword(LoginRequest loginRequest) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("[LAYER: SERVICE] Authenticating user: {}", loginRequest.getEmail());

        // First check if user exists
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException("User not found with email: " + loginRequest.getEmail()));

        // Check if user is active
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new AppException("Account is disabled");
        }

        // Check if user has a password (not OAuth-only user)
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new AppException("Invalid login method");
        }

        try {
            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String jwt = jwtTokenProvider.generateTokenForUser(user.getEmail());

            // Create user info DTO
            UserInfoDto userInfo = UserInfoDto.fromUser(user);

            return new AuthenticationResponse(jwt, userInfo);

        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            throw new AppException("Invalid email or password");
        } catch (org.springframework.security.authentication.DisabledException e) {
            throw new AppException("Account is disabled");
        } catch (org.springframework.security.authentication.LockedException e) {
            throw new AppException("Account is locked");
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

    public AuthenticationResponse registerWithEmailPassword(RegisterRequest registerRequest) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("[LAYER: SERVICE] Registering new user: {}", registerRequest.getEmail());

        // First check if user exists
        Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new AppException("User already exists with email: " + registerRequest.getEmail());
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(registerRequest.getPassword());
        newUser.setFullName(registerRequest.getFullName());
        newUser.setRole(registerRequest.getRole());
        newUser.setStatus(User.UserStatus.ACTIVE);

        User savedUser = userRepository.save(newUser);

        // Generate JWT token
        String jwt = jwtTokenProvider.generateTokenForUser(savedUser.getEmail());

        // Create user info DTO
        UserInfoDto userInfo = UserInfoDto.fromUser(savedUser);

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
                .orElseThrow(() -> new AppException("User not found with email: " + email));

        return UserInfoDto.fromUser(user);
    }

    /**
     * Create Spring Security Authentication object from User
     */
    public Authentication createAuthentication(User user) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                authorities);
    }
}
