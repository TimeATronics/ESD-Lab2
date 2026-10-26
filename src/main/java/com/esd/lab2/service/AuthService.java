package com.esd.lab2.service;

import com.esd.lab2.dto.SignupRequest;
import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.entity.Role;
import com.esd.lab2.entity.User;
import com.esd.lab2.exception.DuplicateResourceException;
import com.esd.lab2.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Business logic for authentication.
 *
 * Slice 1: signup only.
 * Slice 2 will add login() and JWT issuing to this class.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     *
     * Steps:
     *  1. Normalise the email: trim whitespace and convert to lowercase.
     *  2. Reject an email that already exists with DuplicateResourceException (409).
     *  3. Hash the password with the injected PasswordEncoder bean (BCrypt).
     *  4. Save a new User with Role.STUDENT. The role is never read from the request.
     *  5. Return a UserResponse; never return the User entity.
     */
    public UserResponse signup(SignupRequest request) {
        // 1. Normalise so that Alice@test.com and alice@test.com are the same user.
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        // 2. Duplicate check must use the normalised email.
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }

        // 3. Hash the password using the injected encoder. Never store plaintext.
        String passwordHash = passwordEncoder.encode(request.password());

        // 4. Build and save the entity. Signup always creates a STUDENT.
        User user = new User(email, passwordHash, request.fullName(), Role.STUDENT);
        User saved = userRepository.save(user);

        // 5. Map to the public response DTO.
        return toResponse(saved);
    }

    // Mapping
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
