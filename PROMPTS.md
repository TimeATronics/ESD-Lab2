# PROMPTS.md - AI Prompt Log

Record every prompt you send to the AI **starting from Slice 2**.  
Slice 1 is done manually - no AI. See `README.md` for the prompt template structure.

> **Rules:**
> - One prompt (+ follow-ups) per slice
> - Use the structured template from the README - free-form prompts are not allowed
> - Do not back-fill this file after the lab

---

## Example - How a good S1 prompt looks (for reference only, S1 is done manually)

```
CONTEXT
Spring Boot 3.x, Spring Security 6, Java 17+, jjwt 0.12.x. No other libraries.
Package layout:
  com.esd.lab2
  ├── config/       SecurityConfig.java, AdminSeeder.java
  ├── controller/   CourseController.java
  ├── service/      CourseService.java
  ├── dto/          CourseRequest.java, CourseResponse.java
  ├── entity/       User.java, Role.java (enum), Course.java
  ├── repository/   UserRepository.java, CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java,
  │                 DuplicateResourceException.java, InvalidCredentialsException.java,
  │                 ResourceNotFoundException.java
  └── security/     JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java


TASK
Implement only POST /api/auth/signup.
Request: SignupRequest { email (valid email, required), password (required, 8–64 chars),
         fullName (required, not blank, max 100) }.
Response: 201 with UserResponse { id, email, fullName, role, createdAt }.
The new user's role is always STUDENT, even if the request contains a role field.
Store email trimmed and lowercased. Hash the password with the existing PasswordEncoder bean.
If the email already exists, throw DuplicateResourceException.

ALLOWED TO CREATE OR CHANGE
dto/SignupRequest.java, dto/UserResponse.java,
service/AuthService.java (signup method only),
controller/AuthController.java (signup endpoint only)

RULES
- Do not create or modify any file not listed above.
- Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use jakarta.validation annotations for input validation.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

---

## Slice 2 - Login & JWT (S2)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.2.5, Spring Security 6, Java 17, jjwt 0.12.6. No other libraries.
Package layout:
  com.esd.lab2
  ├── config/       SecurityConfig.java, AdminSeeder.java
  ├── controller/   AuthController.java, CourseController.java
  ├── service/      AuthService.java, CourseService.java
  ├── dto/          SignupRequest.java, UserResponse.java, CourseRequest.java, CourseResponse.java
  ├── entity/       User.java, Role.java, Course.java
  ├── repository/   UserRepository.java, CourseRepository.java
  ├── exception/    ApiError.java, GlobalExceptionHandler.java,
  │                 DuplicateResourceException.java, InvalidCredentialsException.java,
  │                 ResourceNotFoundException.java
  └── security/     JsonAuthenticationEntryPoint.java, JsonAccessDeniedHandler.java

Existing code you must use (do not rewrite it):
- AuthService.signup(SignupRequest) normalises email, checks duplicates, hashes with PasswordEncoder, saves Role.STUDENT, returns UserResponse.
- UserRepository.findByEmail(String) exists.
- SecurityConfig currently permits all requests. Do not modify it.
- application.yml defines jwt.secret (Base64) and jwt.expiration-seconds.

TASK
Implement only POST /api/auth/login and the JWT generation needed for it.
Contract:
  POST /api/auth/login is public.
  Request: LoginRequest { email: required and valid email, password: required }.
  Success: 200 LoginResponse { accessToken, tokenType: "Bearer", expiresIn: <seconds> }.
  Errors: 400 for invalid request body; 401 with "Invalid email or password" for both unknown email and wrong password.

Behaviour:
- Look up the user by email trimmed and lowercased.
- Verify the password with the injected PasswordEncoder.matches(rawPassword, passwordHash).
- If the user does not exist or the password does not match, throw InvalidCredentialsException("Invalid email or password"). Use the same exception and message in both cases.
- Generate a JWT with sub = user email, role = user role name, jti = random UUID, iat = current time, exp = current time + jwt.expiration-seconds.
- Sign with HS256 using a SecretKey built from Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwt.secret)).
- Read jwt.secret and jwt.expiration-seconds with @Value. Do not hardcode the secret or expiry.
- LoginResponse.expiresIn must come from jwt.expiration-seconds, not a hardcoded number.
- Do not add token parsing or validation. That is Slice 3.

ALLOWED TO CREATE OR CHANGE
dto/LoginRequest.java (new)
dto/LoginResponse.java (new)
security/JwtService.java (new; generateToken and getExpirationSeconds only)
service/AuthService.java (add login method and JwtService dependency only)
controller/AuthController.java (add login endpoint only)

RULES
- Do not create or modify any file not listed above.
- Do not modify SecurityConfig.
- Use the existing exceptions in com.esd.lab2.exception. Do not create new error classes.
- Use DTOs for request and response. Never return an entity from a controller.
- Use jakarta.validation annotations for input validation.
- Use only APIs that exist in Spring Security 6 and jjwt 0.12. No deprecated methods.
- Do not use AuthenticationManager or UserDetailsService.
- Do not put a password or password hash in the token claims.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
No follow-up was needed. The output matched the contract and review checklist.
```

---

## Slice 3 - JWT Filter & Protected API (S3)

**Prompt 1:**

```
CONTEXT
Spring Boot 3.2.5, Spring Security 6, Java 17, jjwt 0.12.6. No other libraries.
Current relevant code:
- JwtService.generateToken(User) issues HS256 tokens with sub, role, jti, iat, exp.
- SecurityConfig is temporary and permits all requests.
- JsonAuthenticationEntryPoint and JsonAccessDeniedHandler are provided.
- AuthController has signup and login.
- CourseController has GET and POST /api/courses.
- UserRepository.findByEmail(String) exists.
- UserResponse has id, email, fullName, role, createdAt.

TASK
Implement JWT authentication for protected endpoints.
Contract:
  Public:
    POST /api/auth/signup
    POST /api/auth/login
    GET /api/courses
  Protected:
    GET /api/users/me
    All other requests require authentication.
  GET /api/courses without a token returns 200.
  GET /api/users/me without a valid token returns 401 with an ApiError JSON body.
  GET /api/users/me with a valid token returns 200 UserResponse.

Implement:
1. Add JwtService.parseToken(String) using Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().
2. Create JwtAuthenticationFilter extends OncePerRequestFilter.
   - Read Authorization: Bearer <token>.
   - If absent, continue the chain unchanged.
   - If present and valid, set UsernamePasswordAuthenticationToken in SecurityContextHolder.
   - Principal is the token subject (email).
   - Authority is ROLE_<role>.
   - On JwtException or IllegalArgumentException, clear the context and continue the chain.
   - Do not write a response from the filter.
3. Replace the temporary SecurityConfig.filterChain bean.
   - CSRF disabled.
   - Session policy STATELESS.
   - Permit only POST /api/auth/signup, POST /api/auth/login, GET /api/courses.
   - All other requests authenticated.
   - Wire JsonAuthenticationEntryPoint and JsonAccessDeniedHandler via exceptionHandling.
   - Register the JWT filter before UsernamePasswordAuthenticationFilter.
   - Construct the filter in SecurityConfig; do not expose it as a separate servlet filter bean.
4. Create UserController with GET /api/users/me.
   - Read the email from Authentication.
   - Load the user by email.
   - Return UserResponse, never the User entity.

ALLOWED TO CREATE OR CHANGE
security/JwtService.java (add parseToken only)
security/JwtAuthenticationFilter.java (new)
config/SecurityConfig.java (filterChain bean only)
controller/UserController.java (new)

RULES
- Do not create or modify any file not listed above.
- Use Spring Security 6 style only: SecurityFilterChain, lambda DSL, requestMatchers, authorizeHttpRequests.
- Do not use WebSecurityConfigurerAdapter, antMatchers, authorizeRequests, or .and().
- Do not add UserDetailsService, AuthenticationManager, sessions, or CORS.
- Use the existing exceptions and handlers. Do not create new error classes.
- Use DTOs for responses. Never return an entity from a controller.
- Filter must not swallow an exception and then authenticate anyway.
- If something is unclear, add a TODO comment instead of guessing.

OUTPUT
Each file in full, with its path. Then a list of anything you assumed.
```

**Follow-up (if any):**

```
No follow-up was needed. The output matched the contract and review checklist.
```

---

## Slice 4 - Role Restriction (S4)

**Prompt 1:**

```
(paste your prompt here)
```

**Follow-up (if any):**

```
(paste here)
```

---

## Slice 5 - Logout *(stretch, S5)*

**Prompt 1:**

```
(paste your prompt here)
```

**Follow-up (if any):**

```
(paste here)
```
