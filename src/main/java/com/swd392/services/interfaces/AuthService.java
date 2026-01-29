package com.swd392.services.interfaces;

import com.swd392.dtos.authDTO.AuthenticationResponse;
import com.swd392.dtos.authDTO.LoginRequest;
import com.swd392.dtos.authDTO.RegisterRequest;
import com.swd392.dtos.userDTO.UserInfoDto;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {
    
    /**
     * Authenticate user with email and password
     * @param loginRequest Login credentials
     * @return Authentication response with JWT token and user info
     */
    AuthenticationResponse authenticateWithEmailPassword(LoginRequest loginRequest);
    
    /**
     * Register new user with email and password
     * @param registerRequest Registration details
     * @return Authentication response with JWT token and user info
     */
    AuthenticationResponse registerWithEmailPassword(RegisterRequest registerRequest);
    
    /**
     * Authenticate user with Google OAuth2
     * @param oauth2User OAuth2 user details from Google
     * @return Authentication response with JWT token and user info
     */
    AuthenticationResponse authenticateWithGoogle(OAuth2User oauth2User);
    
    /**
     * Get current authenticated user information
     * @return Current user information
     */
    UserInfoDto getCurrentUser();
}
