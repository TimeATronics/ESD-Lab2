package com.esd.lab2.controller;

import com.esd.lab2.dto.SignupRequest;
import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 *
 * Slice 1: POST /api/auth/signup.
 * Slice 2 will add POST /api/auth/login.
 * Slice 5 will add POST /api/auth/logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * A1 - POST /api/auth/signup
     * Public endpoint. Returns 201 Created with the public UserResponse.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
