package com.swd392.services.impl;

import com.swd392.configs.RequestContext;
import com.swd392.entities.User;
import com.swd392.exceptions.AppException;
import com.swd392.repositories.UserRepository;
import com.swd392.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        RequestContext.setCurrentLayer("SERVICE");
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        RequestContext.setCurrentLayer("SERVICE");
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("Creating new user: {}", user.getEmail());
        
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new AppException("User already exists with email: " + user.getEmail(), HttpStatus.CONFLICT);
        }
        
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        RequestContext.setCurrentLayer("SERVICE");
        log.info("Updating user: {}", user.getEmail());
        
        if (user.getUserId() == null) {
            throw new AppException("User ID cannot be null for update", HttpStatus.BAD_REQUEST);
        }
        
        if (!userRepository.existsById(user.getUserId())) {
            throw new AppException("User not found with ID: " + user.getUserId(), HttpStatus.NOT_FOUND);
        }
        
        return userRepository.save(user);
    }
}
