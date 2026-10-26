package com.esd.lab2.dto;

import com.esd.lab2.entity.Role;

import java.time.Instant;

/**
 * Public response DTO for a user.
 *
 * Returned by signup and, later, by GET /api/users/me.
 * It deliberately contains no passwordHash field.
 */
public record UserResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        Instant createdAt
) {}
