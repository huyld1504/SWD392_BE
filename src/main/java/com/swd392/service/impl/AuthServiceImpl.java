package com.swd392.service.impl;


import com.swd392.configs.JwtUtil;
import com.swd392.dto.LoginRequestDTO;
import com.swd392.dto.LoginResponse;
import com.swd392.dto.RegisterRequestDTO;
import com.swd392.entity.Users;
import com.swd392.entity.Wallet;
import com.swd392.repository.UserRepository;
import com.swd392.repository.WalletRepository;
import com.swd392.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final WalletRepository walletRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepo,
                           WalletRepository walletRepo,
                           PasswordEncoder encoder,
                           JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.walletRepo = walletRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public void register(RegisterRequestDTO req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email exists");
        }

        Users user = new Users();
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(Users.Role.STUDENT);
        userRepo.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        walletRepo.save(wallet);
    }

    @Override
    public LoginResponse login(LoginRequestDTO req) {
        Users user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid login"));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid login");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token);
    }
}