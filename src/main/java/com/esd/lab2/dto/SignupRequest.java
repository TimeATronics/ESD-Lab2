package com.esd.lab2.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/auth/signup.
 *
 * Contract:
 *   - email: required, valid email address
 *   - password: required, 8-64 characters
 *   - fullName: required, not blank, at most 100 characters
 *
 * There is no "role" field here. Signup always creates a STUDENT,
 * so a caller cannot self-assign ADMIN.
 */

public record SignupRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        String password,

        @NotBlank(message = "Full name is required")
        @Size(max = 100, message = "Full name must be at most 100 characters")
        String fullName
) {}
