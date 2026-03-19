package com.swd392.configs;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handler for OAuth2 authentication failures.
 * This handler redirects the user back to the frontend with an error message
 * when OAuth2 authentication fails.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.authorized-redirect-uri:http://localhost:3000/auth/callback}")
    private String authorizedRedirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage = determineErrorMessage(exception);
        
        log.error("OAuth2 authentication failed: {}", errorMessage, exception);

        // Build redirect URL with error parameter
        String targetUrl = buildErrorRedirectUrl(errorMessage);

        // Redirect to frontend with error
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Determines the appropriate error message based on the exception type
     */
    private String determineErrorMessage(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
            
            // Handle specific OAuth2 error codes
            String errorCode = error.getErrorCode();
            switch (errorCode) {
                case "access_denied":
                    return "Bạn đã từ chối quyền truy cập. Vui lòng chấp nhận để tiếp tục.";
                case "unauthorized_client":
                    return "Ứng dụng không được ủy quyền. Vui lòng liên hệ quản trị viên.";
                case "invalid_request":
                    return "Yêu cầu OAuth2 không hợp lệ.";
                case "server_error":
                    return "Lỗi server OAuth2. Vui lòng thử lại sau.";
                case "temporarily_unavailable":
                    return "Dịch vụ OAuth2 tạm thời không khả dụng. Vui lòng thử lại sau.";
                default:
                    return "Đăng nhập với Google thất bại: " + error.getDescription();
            }
        }
        
        // Handle other authentication exceptions
        if (exception.getMessage() != null) {
            return "Xác thực thất bại: " + exception.getMessage();
        }
        
        return "Đã xảy ra lỗi không mong muốn trong quá trình đăng nhập. Vui lòng thử lại.";
    }

    /**
     * Builds the redirect URL with error parameter
     */
    private String buildErrorRedirectUrl(String errorMessage) {
        return authorizedRedirectUri + "?error=" +
                URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
    }
}
