package com.swd392.service;

import com.swd392.dto.LoginRequestDTO;
import com.swd392.dto.LoginResponse;
import com.swd392.dto.RegisterRequestDTO;

public interface AuthService {
    void register(RegisterRequestDTO req);
    LoginResponse login(LoginRequestDTO req);
}
