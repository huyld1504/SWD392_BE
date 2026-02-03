package com.swd392.service;


import com.swd392.dtos.authDTO.AuthenticationResponse;
import com.swd392.dtos.authDTO.LoginRequest;
import com.swd392.dtos.authDTO.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest req);
    AuthenticationResponse login(LoginRequest req);
}
