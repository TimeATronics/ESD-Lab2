package com.esd.lab2.controller;

import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.entity.User;
import com.esd.lab2.exception.InvalidCredentialsException;
import com.esd.lab2.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints for the currently authenticated user.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * P2 - GET /api/users/me
     * Requires a valid Bearer token.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Invalid email or password"));

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt());

        return ResponseEntity.ok(response);
    }
}
