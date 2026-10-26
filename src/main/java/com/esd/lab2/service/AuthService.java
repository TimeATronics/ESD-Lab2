package com.esd.lab2.service;

import com.esd.lab2.dto.LoginRequest;
import com.esd.lab2.dto.LoginResponse;
import com.esd.lab2.dto.SignupRequest;
import com.esd.lab2.dto.UserResponse;
import com.esd.lab2.entity.Role;
import com.esd.lab2.entity.User;
import com.esd.lab2.exception.DuplicateResourceException;
import com.esd.lab2.exception.InvalidCredentialsException;
import com.esd.lab2.repository.UserRepository;
import com.esd.lab2.security.JwtService;
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

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    /**
     * Authenticates a user and issues a JWT.
     *
     * The same exception and message are used for an unknown email and a wrong
     * password, so the response does not reveal which one failed.
     */
    public LoginResponse login(LoginRequest request) {
        // Normalise the email exactly like signup does.
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        // Unknown email and wrong password both produce the same 401 response.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        String accessToken = jwtService.generateToken(user);
        return new LoginResponse(accessToken, "Bearer", jwtService.getExpirationSeconds());
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
