package com.swd392.configs;

import com.swd392.entities.User;
import com.swd392.repositories.UserRepository;
import com.swd392.services.interfaces.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Database initialization configuration.
 * Creates default test users and their MAIN wallets if they don't exist.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;

    @Bean
    public CommandLineRunner initData() {
        return args -> {

            // ==================== STUDENT ====================
            createUserIfNotExists(
                    "student@test.com",
                    "Test Student",
                    "password123",
                    User.UserRole.STUDENT
            );

            // ==================== LECTURE ====================
            createUserIfNotExists(
                    "teacher@test.com",
                    "Test Teacher",
                    "password123",
                    User.UserRole.LECTURE
            );

            // ==================== ADMIN ====================
            createUserIfNotExists(
                    "admin@test.com",
                    "Test Admin",
                    "admin123",
                    User.UserRole.ADMIN
            );

            // ==================== SYSTEM WALLET ====================
            walletService.initializeSystemWallet();

            log.info("✅ Database initialization completed!");
        };
    }

    /**
     * Create a test user if not exists, and ensure they have a MAIN wallet.
     * This mirrors the registration flow in AuthServiceImpl.
     */
    private void createUserIfNotExists(String email, String fullName, String password, User.UserRole role) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // Create new user
            user = new User();
            user.setEmail(email);
            user.setFullName(fullName);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setStatus(User.UserStatus.ACTIVE);
            user.setProvider("local");
            user = userRepository.save(user);

            log.info("✅ Created test {} user: {} / {}", role, email, password);
        } else {
            log.info("ℹ️  User already exists: {} ({})", email, role);
        }

        // Ensure user has a MAIN wallet (idempotent — skips if already exists)
        walletService.createDefaultWallet(user);
    }
}
