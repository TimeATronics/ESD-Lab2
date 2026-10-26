package com.esd.lab2.controller;

import com.esd.lab2.dto.LoginRequest;
import com.esd.lab2.dto.LoginResponse;
import com.esd.lab2.dto.SignupRequest;
import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.security.JwtService;
import com.esd.lab2.security.TokenDenylist;
import com.esd.lab2.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints.
 *
 * Slice 1: POST /api/auth/signup.
 * Slice 2: POST /api/auth/login.
 * Slice 5: POST /api/auth/logout.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenDenylist tokenDenylist;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          TokenDenylist tokenDenylist) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.tokenDenylist = tokenDenylist;
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

    /**
     * A2 - POST /api/auth/login
     * Public endpoint. Returns 200 OK with a Bearer token on success.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * A3 - POST /api/auth/logout
     * Requires a valid Bearer token. Revokes its jti and returns 204.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String token = authorization.substring("Bearer ".length());
        Claims claims = jwtService.parseToken(token);
        tokenDenylist.revoke(claims.getId(), claims.getExpiration().toInstant());
        return ResponseEntity.noContent().build();
    }
}
