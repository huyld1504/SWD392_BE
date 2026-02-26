package com.swd392.services.interfaces;

import com.swd392.dtos.userDTO.ChangePasswordRequest;
import com.swd392.dtos.userDTO.ResetPasswordRequest;
import com.swd392.entities.User;

import java.util.Optional;

public interface UserService {
    
    /**
     * Find user by email
     * @param email User email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found
     */
    Optional<User> findById(Long id);
    
    /**
     * Create new user
     * @param user User entity to create
     * @return Created user
     */
    User createUser(User user);
    
    /**
     * Update existing user
     * @param user User entity to update
     * @return Updated user
     */
    User updateUser(User user);

    /**
     * Change user password
     * @param email User email
     * @param request Change password request
     */
    void changePassword(String email, ChangePasswordRequest request);

    /******* Reset Password ****** */
    void resetPassword(ResetPasswordRequest request);

    /******* Forgot Password ****** */
    void forgotPassword(String email);
}
