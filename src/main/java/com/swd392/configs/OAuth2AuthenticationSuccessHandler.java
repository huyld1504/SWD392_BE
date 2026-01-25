package com.swd392.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swd392.dtos.authDTO.AuthenticationResponse;
import com.swd392.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:3000/auth/callback}")
    private String authorizedRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        try {
            // Authenticate with Google and get JWT token
            AuthenticationResponse authResponse = authService.authenticateWithGoogle(oauth2User);

            // Redirect to frontend with token
            String targetUrl = authorizedRedirectUri + "?token=" +
                    URLEncoder.encode(authResponse.getToken(), StandardCharsets.UTF_8) +
                    "&user=" + URLEncoder.encode(objectMapper.writeValueAsString(authResponse.getUser()), StandardCharsets.UTF_8);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            // Redirect to frontend with error
            String targetUrl = authorizedRedirectUri + "?error=" +
                    URLEncoder.encode("Authentication failed: " + e.getMessage(), StandardCharsets.UTF_8);

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}
