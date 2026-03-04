package com.swd392.services.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.swd392.configs.RequestContext;
import com.swd392.dtos.authDTO.AuthenticationResponse;
import com.swd392.dtos.authDTO.LoginRequest;
import com.swd392.dtos.authDTO.RegisterRequest;
import com.swd392.dtos.userDTO.UserInfoDto;
import com.swd392.entities.User;
import com.swd392.exceptions.AppException;
import com.swd392.repositories.UserRepository;
import com.swd392.services.GoogleTokenVerifierService;
import com.swd392.services.JwtTokenProvider;
import com.swd392.services.interfaces.AuthService;
import com.swd392.services.interfaces.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final WalletService walletService;
    private final GoogleTokenVerifierService googleTokenVerifierService;

    @Override
    @Transactional
    public AuthenticationResponse authenticateWithEmailPassword(LoginRequest loginRequest) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("Authenticating user with email: {}", loginRequest.getEmail());

        // Verify user exists
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException("User not found with email: " + loginRequest.getEmail(),
                        HttpStatus.NOT_FOUND));

        // Check user status
        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new AppException("User account is banned", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new AppException("User account is suspended", HttpStatus.FORBIDDEN);
        }

        // Authenticate
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Return response
            UserInfoDto userInfo = UserInfoDto.fromUser(user);
            log.info("User authenticated successfully: {}", loginRequest.getEmail());

            return new AuthenticationResponse(token, userInfo);

        } catch (Exception e) {
            log.error("Authentication failed for email: {}", loginRequest.getEmail(), e);
            throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional
    public AuthenticationResponse registerWithEmailPassword(RegisterRequest registerRequest) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("Registering new user with email: {}", registerRequest.getEmail());

        // Check if user already exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new AppException("User already exists with email: " + registerRequest.getEmail(),
                    HttpStatus.CONFLICT);
        }

        // Create new user
        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setFullName(registerRequest.getFullName());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : User.UserRole.STUDENT);
        newUser.setStatus(User.UserStatus.ACTIVE);
        newUser.setProvider("local");

        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully: {}", savedUser.getEmail());

        // Create default MAIN wallet (BLUE currency) for the new user
        walletService.createDefaultWallet(savedUser);

        // Generate JWT token
        String token = jwtTokenProvider.generateTokenForUser(savedUser.getEmail());

        // Return response
        UserInfoDto userInfo = UserInfoDto.fromUser(savedUser);
        return new AuthenticationResponse(token, userInfo);
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticateWithGoogle(OAuth2User oauth2User) {
        RequestContext.setCurrentLayer("SERVICE");

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");
        String providerId = oauth2User.getAttribute("sub");

        log.info("Authenticating user with Google OAuth2: {}", email);

        if (email == null) {
            throw new AppException("Email not found in Google OAuth2 response", HttpStatus.BAD_REQUEST);
        }

        // Find or create user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Creating new user from Google OAuth2: {}", email);
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(name != null ? name : email);
                    newUser.setProvider("google");
                    newUser.setProviderId(providerId);
                    newUser.setAvatarUrl(picture);
                    newUser.setRole(User.UserRole.STUDENT);
                    newUser.setStatus(User.UserStatus.ACTIVE);
                    User savedUser = userRepository.save(newUser);

                    // Create default MAIN wallet (BLUE currency) for the new Google user
                    walletService.createDefaultWallet(savedUser);

                    return savedUser;
                });

        // Check user status
        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new AppException("User account is banned", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new AppException("User account is suspended", HttpStatus.FORBIDDEN);
        }

        // Update user info if needed
        if (user.getProvider() == null || !user.getProvider().equals("google")) {
            user.setProvider("google");
            user.setProviderId(providerId);
        }
        if (picture != null && !picture.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(picture);
        }
        if (name != null && !name.equals(user.getFullName())) {
            user.setFullName(name);
        }
        userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateTokenForUser(user.getEmail());

        // Return response
        UserInfoDto userInfo = UserInfoDto.fromUser(user);
        log.info("User authenticated successfully with Google: {}", email);

        return new AuthenticationResponse(token, userInfo);
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticateWithGoogleIdToken(String idTokenString) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info(
                "\n    \u251c\u2500 SERVICE \u2500 authenticateWithGoogleIdToken\n    \u2502 Verifying Google ID Token...");

        // 1. Verify the ID token with Google
        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(idTokenString);
        if (payload == null) {
            throw new AppException("Invalid or expired Google ID token", HttpStatus.UNAUTHORIZED);
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        String providerId = payload.getSubject();
        boolean emailVerified = payload.getEmailVerified();

        log.info(
                "\n    \u2502 Token verified\n    \u2502 Email    : {}\n    \u2502 Name     : {}\n    \u2502 Verified : {}",
                email, name, emailVerified);

        if (email == null || !emailVerified) {
            throw new AppException("Google account email is not verified", HttpStatus.BAD_REQUEST);
        }

        // 2. Find or create user (same logic as web OAuth2)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("    \u2502 Creating new user from Google Android: {}", email);
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(name != null ? name : email);
                    newUser.setProvider("google");
                    newUser.setProviderId(providerId);
                    newUser.setAvatarUrl(picture);
                    newUser.setRole(User.UserRole.STUDENT);
                    newUser.setStatus(User.UserStatus.ACTIVE);
                    User savedUser = userRepository.save(newUser);

                    // Create default wallet for new user
                    walletService.createDefaultWallet(savedUser);

                    return savedUser;
                });

        // 3. Check user status
        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new AppException("User account is banned", HttpStatus.FORBIDDEN);
        }
        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new AppException("User account is suspended", HttpStatus.FORBIDDEN);
        }

        // 4. Update user info if changed
        boolean updated = false;
        if (user.getProvider() == null || !user.getProvider().equals("google")) {
            user.setProvider("google");
            user.setProviderId(providerId);
            updated = true;
        }
        if (picture != null && !picture.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(picture);
            updated = true;
        }
        if (name != null && !name.equals(user.getFullName())) {
            user.setFullName(name);
            updated = true;
        }
        if (updated) {
            userRepository.save(user);
        }

        // 5. Generate JWT token
        String token = jwtTokenProvider.generateTokenForUser(user.getEmail());

        UserInfoDto userInfo = UserInfoDto.fromUser(user);
        log.info(
                "\n    \u2514\u2500 SERVICE \u2500 authenticateWithGoogleIdToken\n      Status : SUCCESS\n      User   : {}\n      Role   : {}",
                email, user.getRole());

        return new AuthenticationResponse(token, userInfo);
    }

    @Override
    public UserInfoDto getCurrentUser() {
        RequestContext.setCurrentLayer("SERVICE");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException("User not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String email = authentication.getName();
        log.info("Getting current user info for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        return UserInfoDto.fromUser(user);
    }
}