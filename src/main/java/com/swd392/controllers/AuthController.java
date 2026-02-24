package com.swd392.controllers;

import com.swd392.configs.RequestContext;
import com.swd392.dtos.authDTO.AuthenticationResponse;
import com.swd392.dtos.authDTO.LoginRequest;
import com.swd392.dtos.authDTO.RegisterRequest;
import com.swd392.dtos.common.ApiResponse;
import com.swd392.dtos.userDTO.UserInfoDto;
import com.swd392.services.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

        private final AuthService authService;

        /**
         * Login with email and password
         */
        @Operation(summary = "Login with email and password", description = "Authenticate user with email and password credentials. Returns JWT token and user information.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid credentials", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
        })
        @PostMapping("/login")
        public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
                RequestContext.setCurrentLayer("CONTROLLER");
                log.info("Login attempt for email: {}", loginRequest.getEmail());
                AuthenticationResponse result = authService.authenticateWithEmailPassword(loginRequest);
                log.info("Login successful for email: {}", loginRequest.getEmail());
                return ApiResponse.<AuthenticationResponse>builder()
                                .success(true)
                                .message("Login successful")
                                .data(result)
                                .build();
        }

        /**
         * Get current user information
         */
        @Operation(summary = "Get current user information", description = "Retrieve the authenticated user's profile information. Requires valid JWT token.", security = @SecurityRequirement(name = "Bearer Authentication"))
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User information retrieved successfully", content = @Content(schema = @Schema(implementation = UserInfoDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content)
        })
        @GetMapping("/me")
        @PreAuthorize("hasRole('STUDENT') or hasRole('LECTURE') or hasRole('ADMIN')")
        public ApiResponse<UserInfoDto> getCurrentUser() {
                RequestContext.setCurrentLayer("CONTROLLER");
                UserInfoDto userInfo = authService.getCurrentUser();
                return ApiResponse.<UserInfoDto>builder()
                                .success(true)
                                .message("User information retrieved successfully")
                                .data(userInfo)
                                .build();
        }

        /**
         * Initiate Google OAuth2 login
         * This endpoint will redirect to Google for authentication
         */
        @Operation(summary = "Initiate Google OAuth2 login", description = "Redirects to Google for OAuth2 authentication. Users will be redirected to Google login page.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Redirect to Google OAuth2", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request", content = @Content)
        })
        @GetMapping("/oauth2/google")
        public ResponseEntity<Void> loginWithGoogle() {
                return ResponseEntity.status(HttpStatus.FOUND)
                                .header("Location", "/oauth2/authorization/google")
                                .build();
        }

        @Operation(summary = "Register with email and password", description = "Register a new user with email and password credentials.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Registration successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists", content = @Content)
        })
        @PostMapping("/register")
        public ApiResponse<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
                RequestContext.setCurrentLayer("CONTROLLER");
                log.info("Registration attempt for email: {}", registerRequest.getEmail());
                AuthenticationResponse result = authService.registerWithEmailPassword(registerRequest);
                log.info("Registration successful for email: {}", registerRequest.getEmail());
                return ApiResponse.<AuthenticationResponse>builder()
                                .success(true)
                                .message("Registration successful")
                                .data(result)
                                .build();
        }
}
