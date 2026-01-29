package com.swd392.configs;

import com.swd392.entities.User;
import com.swd392.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Database initialization configuration
 * Creates default test users if they don't exist
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Create test student user if not exists
            if (userRepository.findByEmail("student@test.com").isEmpty()) {
                User student = new User();
                student.setEmail("student@test.com");
                student.setFullName("Test Student");
                student.setPassword(passwordEncoder.encode("password123"));
                student.setRole(User.UserRole.STUDENT);
                student.setStatus(User.UserStatus.ACTIVE);
                student.setProvider("local");

                userRepository.save(student);
                log.info("✅ Created test student user: student@test.com / password123");
            }

            // Create test teacher user if not exists
            if (userRepository.findByEmail("teacher@test.com").isEmpty()) {
                User teacher = new User();
                teacher.setEmail("teacher@test.com");
                teacher.setFullName("Test Teacher");
                teacher.setPassword(passwordEncoder.encode("password123"));
                teacher.setRole(User.UserRole.LECTURE);
                teacher.setStatus(User.UserStatus.ACTIVE);
                teacher.setProvider("local");

                userRepository.save(teacher);
                log.info("Created test teacher user: teacher@test.com / password123");
            }

            // Create test admin user if not exists
            if (userRepository.findByEmail("admin@test.com").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@test.com");
                admin.setFullName("Test Admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.UserRole.ADMIN);
                admin.setStatus(User.UserStatus.ACTIVE);
                admin.setProvider("local");

                userRepository.save(admin);
                log.info("Created test admin user: admin@test.com / admin123");
            }

            log.info("Database initialization completed!");
        };
    }
}
