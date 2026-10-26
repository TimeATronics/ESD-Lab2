package com.esd.lab2.dto;

/**
 * Response body for a successful POST /api/auth/login.
 *
 * accessToken : signed JWT to send as "Authorization: Bearer <token>"
 * tokenType   : always "Bearer"
 * expiresIn   : token lifetime in seconds, read from jwt.expiration-seconds
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
