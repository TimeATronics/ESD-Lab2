# REVIEW.md - AI Output Review Log

Complete **one review entry per slice** before moving to the next.  


---

## Slice 1 - Signup (S1)

> S1 is the manual slice in the lab. No AI prompt is recorded in `PROMPTS.md` for this slice.

### A. Bucket Check
List every class and method produced for this slice. Tag each one:  
`Controller` / `Service` / `Repository` / `DTO` / `Security` / `Config` / `Exception`

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `dto/SignupRequest.java` - validation annotations | DTO | Yes |
| `dto/UserResponse.java` - public response fields | DTO | Yes |
| `service/AuthService.java` - `signup()` and `toResponse()` | Service | Yes |
| `controller/AuthController.java` - `signup()` | Controller | Yes |

> **Important:** Anything **not** in the allowed list → **reject, re-prompt**. Don't keep it because it works.

### B. Contract Check
Compare against API contract row A1 line by line.

| Check | Pass / Fail |
|---|---|
| Path is `POST /api/auth/signup` | Pass |
| Returns **201** | Pass |
| Response shape: `{ id, email, fullName, role, createdAt }` | Pass |
| No `passwordHash` field in response | Pass |
| Role in response is always `STUDENT` | Pass: verified with a request containing `"role":"ADMIN"` |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| `@Valid` on controller parameter | Pass |
| `@Email`, `@NotBlank`, `@Size` on DTO fields | Pass |
| Duplicate email throws `DuplicateResourceException` | Pass: verified case-insensitive duplicate |
| No new error/response wrapper class invented | Pass |
| No stack trace or exception message leaked to client | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Password hashed via injected `PasswordEncoder` (not `new BCryptPasswordEncoder()`) | Pass: stored hashes start with `$2a$10$` |
| `SignupRequest` has no `role` field, or it is ignored | Pass: no `role` field; extra JSON property is ignored |
| Duplicate check uses normalised (lowercased/trimmed) email | Pass |
| AI did **not** touch `SecurityConfig`, add JWT class, or add `UserDetailsService` | Pass |

### E. Understanding Questions
*Answer in your own words (1–2 lines each)*

1. Where exactly is the password hashed, and why is the same password stored as a different string for two users?

   > The password is hashed in `AuthService.signup()` using the injected `PasswordEncoder` bean (`BCryptPasswordEncoder`). BCrypt generates a random salt for each hash, so the same password produces a different stored string each time.

2. What would happen if `SignupRequest` accepted a `role` field and the AI used it?

   > A caller could request `ADMIN` during signup and escalate their own privileges. Signup must always assign `Role.STUDENT` and ignore any role supplied by the client.

3. Which class turns `DuplicateResourceException` into a 409 response?

   > `GlobalExceptionHandler.handleDuplicate()` maps `DuplicateResourceException` to HTTP 409 with the standard `ApiError` body.

### F. Decision

| File | Decision | Reason (if modified/rejected) |
|---|---|---|
| `dto/SignupRequest.java` | Accepted | Contains the required validation annotations and no `role` field. |
| `dto/UserResponse.java` | Accepted | Exposes only public user fields; no password hash. |
| `service/AuthService.java` | Accepted | Normalises email, checks duplicates, hashes with the injected encoder, assigns `STUDENT`, and returns a DTO. |
| `controller/AuthController.java` | Accepted | Uses `@Valid` and returns HTTP 201 Created. |

---

## Slice 2 - Login & JWT (S2)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `dto/LoginRequest.java` - validation annotations | DTO | Yes |
| `dto/LoginResponse.java` - token response fields | DTO | Yes |
| `security/JwtService.java` - `generateToken()`, `getExpirationSeconds()` | Security | Yes |
| `service/AuthService.java` - `login()`, `JwtService` dependency | Service | Yes |
| `controller/AuthController.java` - `login()` | Controller | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| Path is `POST /api/auth/login` | Pass |
| Returns **200** | Pass |
| Response: `{ accessToken, tokenType: "Bearer", expiresIn }` | Pass |
| `expiresIn` matches configured value (not hardcoded) | Pass: returned 3600, from `jwt.expiration-seconds` |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Same 401 message for unknown email and wrong password | Pass: both return `"Invalid email or password"` |
| No `try/catch` swallowing exceptions silently | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| JWT secret comes from `@Value`, no string literal secret | Pass: constructor injection with `@Value("${jwt.secret}")` |
| Uses jjwt 0.12 API: `.subject()`, `.claim()`, `.id()`, `.issuedAt()`, `.expiration()`, `.signWith(key)` | Pass |
| Rejects deprecated: `setSubject`, `setExpiration`, `signWith(SignatureAlgorithm.HS256, secret)`, `Jwts.parserBuilder()` | Pass: none used |
| `jti` is a random UUID (not the user id) | Pass: decoded token contains a UUID |
| No password or hash in token claims | Pass |
| Parse/validate method **not** written yet (that is S3) | Pass: `JwtService` only issues tokens |

### E. Understanding Questions

1. Why must the error message be identical for "no such email" and "wrong password"?

   > It prevents user enumeration. An attacker cannot tell whether an email is registered or whether only the password was wrong.

2. Who can read the claims inside your JWT? Who can change them?

   > Anyone holding the token can Base64-decode the payload and read the claims. Only a party with the signing secret can produce a valid signature, so a client cannot change claims without invalidating the token.

3. What does `jti` give us that `sub` doesn't? (Think about S5.)

   > `sub` identifies the user. `jti` identifies one specific token, so S5 can revoke a single token at logout without invalidating every token issued to that user.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `dto/LoginRequest.java` | Accepted | Matches the login contract validation rules. |
| `dto/LoginResponse.java` | Accepted | Returns the token, `Bearer` type, and configured expiry. |
| `security/JwtService.java` | Accepted | Uses the configured secret and jjwt 0.12 API, with the required claims. |
| `service/AuthService.java` | Accepted | Uses the same 401 message, verifies BCrypt, and issues the token. |
| `controller/AuthController.java` | Accepted | Uses `@Valid` and returns HTTP 200. |

---

## Slice 3 - JWT Filter & Protected API (S3)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `security/JwtService.java` - `parseToken()` | Security | Yes |
| `security/JwtAuthenticationFilter.java` - Bearer token filter | Security | Yes |
| `config/SecurityConfig.java` - `filterChain()` | Config | Yes |
| `controller/UserController.java` - `me()` | Controller | Yes |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `GET /api/courses` works **without** token | Pass |
| `GET /api/users/me` returns **401** with no token | Pass |
| `GET /api/users/me` returns **200** with valid token | Pass |
| 401 body is an `ApiError` JSON object | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Filter does not write a response on error - continues chain | Pass |
| Filter clears SecurityContext on bad token | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Spring Security 6 style: lambda DSL, `requestMatchers`, `authorizeHttpRequests` | Pass |
| Rejected: `WebSecurityConfigurerAdapter`, `antMatchers`, `authorizeRequests`, `.and()` | Pass - none used |
| `permitAll` covers only the three specific public endpoints | Pass |
| `GET /api/courses` is public but `POST /api/courses` is NOT (watch for broad matcher) | Pass - POST without a token returns 401 |
| Signature + expiry verified with `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)` | Pass |
| Authorities use `ROLE_` prefix | Pass |
| Entry point and access-denied handler wired via `exceptionHandling(...)` | Pass |
| No `UserDetailsService`, `AuthenticationManager`, sessions or CORS added | Pass |
| `GET /api/users/me` returns `UserResponse`, not the entity | Pass |

### E. Understanding Questions

1. Why is CSRF protection disabled here, and when would disabling it be wrong?

   > The API authenticates with a Bearer token in the Authorization header, not with cookies. Browsers do not attach that header automatically. Disabling CSRF would be wrong if authentication used cookies or sessions, because a cross-site request could carry the user's credentials.

2. Walk through what happens to a request with an expired token, step by step, until the client sees 401.

   > The filter reads the Bearer token and calls `JwtService.parseToken()`. Signature verification succeeds, but `parseSignedClaims()` throws `ExpiredJwtException`. The filter catches it, clears the SecurityContext, and continues the chain. `AuthorizationFilter` sees an unauthenticated request and lets `ExceptionTranslationFilter` call `JsonAuthenticationEntryPoint`, which writes the 401 ApiError.

3. Why don't 401/403 errors from the filter chain reach `GlobalExceptionHandler`?

   > The filter chain runs before the DispatcherServlet, so those errors never reach a controller or `@RestControllerAdvice`. Spring Security handles them with the authentication entry point and access denied handler.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `security/JwtService.java` | Accepted | `parseToken()` verifies the signature and expiry with jjwt 0.12. |
| `security/JwtAuthenticationFilter.java` | Accepted | Sets the authentication and `ROLE_` authority, clears context on errors, and continues the chain. |
| `config/SecurityConfig.java` | Accepted | Only the three public endpoints are open. The chain is stateless and both JSON handlers are wired. |
| `controller/UserController.java` | Accepted | Loads the user from the token subject and returns `UserResponse`. |

---

## Slice 4 - Role Restriction (S4)

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `config/SecurityConfig.java` - `filterChain()` authorization rule | Config | Yes |
| `controller/CourseController.java` - `createCourse()` | Controller | Already provided, unchanged |
| `service/CourseService.java` - `create()` | Service | Already provided, unchanged |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `POST /api/courses` with no token → **401** | Pass |
| `POST /api/courses` with STUDENT token → **403** | Pass |
| `POST /api/courses` with ADMIN token → **201** | Pass |
| 403 body is an `ApiError` JSON object | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| `@Valid` on the course request body | Pass - provided in `CourseController` |
| Invalid course body returns 400 with field errors | Pass |
| Duplicate course code returns 409 | Pass |
| No new error or response wrapper class | Pass |
| Response is `CourseResponse`, not the entity | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Role naming consistent: `hasRole("ADMIN")` ↔ authority `ROLE_ADMIN` | Pass |
| If `@PreAuthorize` used: `@EnableMethodSecurity` is present | Not used. The rule is in `SecurityConfig`. |
| Rule placed **before** `anyRequest().authenticated()` if in SecurityConfig | Pass |
| Diff is small - AI did not rewrite the whole config | Pass - one matcher added |

### E. Understanding Questions

1. A STUDENT calls `POST /api/courses`. Why does the server return 403 and not 401?

   > The student token is valid, so authentication succeeds. The request then fails the `hasRole("ADMIN")` authorization check. A 401 would mean the request had no valid authentication.

2. Where does the role in the JWT come from, and can the client change it?

   > The role claim is set at login from the user's database role. A client can read the claim, but changing it invalidates the signature. The server only trusts claims from a token signed with its secret.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `config/SecurityConfig.java` | Accepted | Added the ADMIN rule before `anyRequest().authenticated()`. Public matchers and the JWT filter are unchanged. |
| `controller/CourseController.java` | Accepted | Already validates the request, returns 201, and uses `CourseResponse`. No change needed. |
| `service/CourseService.java` | Accepted | Already throws `DuplicateResourceException` for duplicate codes. No change needed. |

---

## Slice 5 - Logout (S5) *(stretch)*

### A. Bucket Check

| File / Method | Tag | In Allowed List? |
|---|---|---|
| `security/TokenDenylist.java` - `revoke()`, `isRevoked()`, `cleanExpired()` | Security | Yes |
| `security/JwtAuthenticationFilter.java` - denylist check and constructor | Security | Yes |
| `controller/AuthController.java` - `logout()` and dependencies | Controller | Yes |
| `config/SecurityConfig.java` - filter wiring | Config | Yes, required to pass the denylist into the filter |

### B. Contract Check

| Check | Pass / Fail |
|---|---|
| `POST /api/auth/logout` returns **204** | Pass |
| Same token returns **401** after logout | Pass |
| Fresh login after logout returns a new working token | Pass |

### C. Validation & Errors

| Check | Pass / Fail |
|---|---|
| Logout without a token returns 401 | Pass |
| Logout with a garbage token returns 401 | Pass |
| No new error or response wrapper class | Pass |
| Filter clears the context and continues the chain | Pass |
| 204 response has no body | Pass |

### D. Security Specifics

| Check | Pass / Fail |
|---|---|
| Logout adds an entry to the server-side denylist (not just tells the client to delete the token) | Pass |
| Denylist keyed by **jti**, not email | Pass |
| Map is `ConcurrentHashMap` (thread-safe) | Pass |
| Logout endpoint is NOT under `permitAll` | Pass |
| Filter checks the denylist and treats a denylisted jti as invalid | Pass |
| (Bonus) Expired entries are removed from the map | Pass |

### E. Understanding Questions

1. Why does logout need server-side state if JWT is "stateless"?

   > A JWT stays valid until its expiry unless the server keeps a record of revoked tokens. The denylist stores the revoked `jti` so the filter can reject that token immediately.

2. What happens to the denylist when the server restarts? What if there are two server instances?

   > An in-memory denylist is cleared on restart, so revoked tokens become usable again until they expire. With multiple instances, each has its own map, so a shared store such as Redis or a database is needed for consistent revocation.

3. Why store the expiry time with each jti?

   > The token's own expiry tells the server when the denylist entry is no longer needed. Cleaning expired entries prevents the map from growing without bound.

### F. Decision

| File | Decision | Reason |
|---|---|---|
| `security/TokenDenylist.java` | Accepted | Uses a `ConcurrentHashMap` keyed by `jti` and removes expired entries. |
| `security/JwtAuthenticationFilter.java` | Accepted | Checks the denylist, clears the context for revoked tokens, and continues the chain. |
| `controller/AuthController.java` | Accepted | Parses the current token, revokes its `jti`, and returns 204. |
| `config/SecurityConfig.java` | Accepted | Passes the denylist into the filter. The logout endpoint stays protected. |

---

## No-AI Change Tasks

Chosen option: 2. Add a public `GET /api/courses/{id}` endpoint with 404 through `ResourceNotFoundException`.

**Files changed:**

| File | What changed and why |
|---|---|
| `controller/CourseController.java` | Added `GET /api/courses/{id}` and called the existing `CourseService.findById()`. |
| `config/SecurityConfig.java` | Allowed public `GET /api/courses/*` while keeping `POST /api/courses` ADMIN-only. |

**Explanation (your own words):**
> The service already had `findById()` and threw `ResourceNotFoundException` when the id was missing, so the controller only needed a new mapping. The security matcher is method-specific and limited to GET, so the ADMIN rule for POST remains in place. A missing id returns 404 with the standard `ApiError`.
