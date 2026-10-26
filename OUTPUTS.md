# API Test Outputs

## Slice 1 - Signup

### 1. Valid signup - expect 201

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/signup" -H "Content-Type: application/json" -d "{\"email\":\"s1.output@test.com\",\"password\":\"password1\",\"fullName\":\"S1 Output\"}"
```

```text
HTTP/1.1 201
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 11:16:21 GMT

{"id":6,"email":"s1.output@test.com","fullName":"S1 Output","role":"STUDENT","createdAt":"2026-10-26T11:16:21.470817200Z"}
```

### 2. Duplicate email, case-insensitive - expect 409

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/signup" -H "Content-Type: application/json" -d "{\"email\":\"S1.OUTPUT@TEST.COM\",\"password\":\"password1\",\"fullName\":\"S1 Output\"}"
```

```text
HTTP/1.1 409
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 11:16:21 GMT

{"timestamp":"2026-10-26T11:16:21.569270300Z","status":409,"error":"Conflict","message":"Email already registered","path":"/api/auth/signup","fieldErrors":null}
```

### 3. Invalid input - expect 400 with field errors

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/signup" -H "Content-Type: application/json" -d "{\"email\":\"bad\",\"password\":\"short\",\"fullName\":\"\"}"
```

```text
HTTP/1.1 400
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 11:16:21 GMT
Connection: close

{"timestamp":"2026-10-26T11:16:21.628424600Z","status":400,"error":"Bad Request","message":"Validation failed","path":"/api/auth/signup","fieldErrors":{"email":"Email must be a valid email address","password":"Password must be between 8 and 64 characters","fullName":"Full name is required"}}
```

### 4. Role escalation attempt - expect 201 with STUDENT

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/signup" -H "Content-Type: application/json" -d "{\"email\":\"s1.output.role@test.com\",\"password\":\"password1\",\"fullName\":\"S1 Output Role\",\"role\":\"ADMIN\"}"
```

```text
HTTP/1.1 201
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 11:16:21 GMT

{"id":7,"email":"s1.output.role@test.com","fullName":"S1 Output Role","role":"STUDENT","createdAt":"2026-10-26T11:16:22.011139Z"}
```

---

## Slice 2 - Login and JWT

### 1. Valid login - expect 200

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"s1.output@test.com\",\"password\":\"password1\"}"
```

```text
HTTP/1.1 200
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 11:23:30 GMT

{"accessToken":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzMS5vdXRwdXRAdGVzdC5jb20iLCJyb2xlIjoiU1RVREVOVCIsImp0aSI6IjE4OTFlOGFlLWU2ZTktNGY1ZC04Yjg3LTc4MjJiOTM4MDU2NiIsImlhdCI6MTc5MzAxMzgxMCwiZXhwIjoxNzkzMDE3NDEwfQ.eU39Rm_Wqxi15LfrQOdltzkDpZ3F-HQBpdHIbsDRs1U","tokenType":"Bearer","expiresIn":3600}
```

Decoded header and claims:

```text
Header:
{"alg":"HS256"}

Payload:
{
  "sub": "s1.output@test.com",
  "role": "STUDENT",
  "jti": "1891e8ae-e6e9-4f5d-8b87-7822b9380566",
  "iat": 1793013810,
  "exp": 1793017410
}

exp - iat = 3600 seconds
```

### 2. Unknown email - expect 401

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"unknown@test.com\",\"password\":\"password1\"}"
```

```text
HTTP/1.1 401
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 11:23:30 GMT

{"timestamp":"2026-10-26T11:23:30.914356900Z","status":401,"error":"Unauthorized","message":"Invalid email or password","path":"/api/auth/login","fieldErrors":null}
```

### 3. Wrong password - expect the same 401 message

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"s1.output@test.com\",\"password\":\"wrongpass\"}"
```

```text
HTTP/1.1 401
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 11:23:31 GMT

{"timestamp":"2026-10-26T11:23:31.330546400Z","status":401,"error":"Unauthorized","message":"Invalid email or password","path":"/api/auth/login","fieldErrors":null}
```

### 4. Invalid login body - expect 400

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"bad\",\"password\":\"\"}"
```

```text
HTTP/1.1 400
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 26 Oct 2026 11:23:30 GMT
Connection: close

{"timestamp":"2026-10-26T11:23:31.401969200Z","status":400,"error":"Bad Request","message":"Validation failed","path":"/api/auth/login","fieldErrors":{"password":"Password is required","email":"Email must be a valid email address"}}
```

### S2 claim checks

- `alg` is `HS256`.
- `sub` is the user email.
- `role` is the user's database role.
- `jti` is a random UUID, not the user id.
- `iat` and `exp` are present and 3600 seconds apart.
- No password or password hash claim is present.
- Unknown email and wrong password return the exact same 401 message.
- `expiresIn` equals the configured 3600 seconds, not a hardcoded number.

---

## Slice 3 - JWT Filter and Protected API

Token used for the valid requests:

```text
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzMS5vdXRwdXRAdGVzdC5jb20iLCJyb2xlIjoiU1RVREVOVCIsImp0aSI6ImZiMjdkOTYwLTAyNGItNDUyNC1iYTMwLWE2NDNmMzBkNThmMCIsImlhdCI6MTc5MzAxNDE1MSwiZXhwIjoxNzkzMDE3NzUxfQ.As2SWNQB0I7KITOXX9PBJZ8KlaTVens1xNEzmufUsZ0
```

### 1. GET /api/courses without token - expect 200

```cmd
curl.exe -sS -i "http://localhost:8080/api/courses"
```

```text
HTTP/1.1 200

[]
```

### 2. GET /api/courses with an invalid token - expect 200

```cmd
curl.exe -sS -i -H "Authorization: Bearer not-a-jwt" "http://localhost:8080/api/courses"
```

```text
HTTP/1.1 200

[]
```

### 3. GET /api/users/me without token - expect 401

```cmd
curl.exe -sS -i "http://localhost:8080/api/users/me"
```

```text
HTTP/1.1 401

{"timestamp":1793014152.166168100,"status":401,"error":"Unauthorized","message":"Authentication required","path":"/api/users/me","fieldErrors":null}
```

### 4. GET /api/users/me with a garbage token - expect 401

```cmd
curl.exe -sS -i -H "Authorization: Bearer not-a-jwt" "http://localhost:8080/api/users/me"
```

```text
HTTP/1.1 401

{"timestamp":1793014152.314509400,"status":401,"error":"Unauthorized","message":"Authentication required","path":"/api/users/me","fieldErrors":null}
```

### 5. GET /api/users/me with a valid token - expect 200

```cmd
curl.exe -sS -i -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzMS5vdXRwdXRAdGVzdC5jb20iLCJyb2xlIjoiU1RVREVOVCIsImp0aSI6ImZiMjdkOTYwLTAyNGItNDUyNC1iYTMwLWE2NDNmMzBkNThmMCIsImlhdCI6MTc5MzAxNDE1MSwiZXhwIjoxNzkzMDE3NzUxfQ.As2SWNQB0I7KITOXX9PBJZ8KlaTVens1xNEzmufUsZ0" "http://localhost:8080/api/users/me"
```

```text
HTTP/1.1 200

{"id":6,"email":"s1.output@test.com","fullName":"S1 Output","role":"STUDENT","createdAt":"2026-10-26T11:16:21.470817Z"}
```

### 6. GET /api/users/me with a tampered signature - expect 401

```cmd
curl.exe -sS -i -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzMS5vdXRwdXRAdGVzdC5jb20iLCJyb2xlIjoiU1RVREVOVCIsImp0aSI6ImZiMjdkOTYwLTAyNGItNDUyNC1iYTMwLWE2NDNmMzBkNThmMCIsImlhdCI6MTc5MzAxNDE1MSwiZXhwIjoxNzkzMDE3NzUxfQ.As2SWNQB0I7KITOXX9PBJZ8KlaTVens1xNEzmufUsZA" "http://localhost:8080/api/users/me"
```

```text
HTTP/1.1 401

{"timestamp":1793014152.514259500,"status":401,"error":"Unauthorized","message":"Authentication required","path":"/api/users/me","fieldErrors":null}
```

### 7. GET /api/users/me with an expired token - expect 401

```cmd
curl.exe -sS -i -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpYXQiOjE3OTMwMDY5NTEsImV4cCI6MTc5MzAxMDU1MSwicm9sZSI6IlNUVURFTlQiLCJzdWIiOiJzMS5vdXRwdXRAdGVzdC5jb20iLCJqdGkiOiJlNGJlZjkzYy03ZjE0LTQwYmQtOGY4Yy02MzkzMmI5YzhhZDIifQ.hqKjulyWFsxAZiX-_P5sg80fnotJdztgGsW_J990fpw" "http://localhost:8080/api/users/me"
```

```text
HTTP/1.1 401

{"timestamp":1793014152.570472400,"status":401,"error":"Unauthorized","message":"Authentication required","path":"/api/users/me","fieldErrors":null}
```

### 8. POST /api/courses without token - expect 401

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/courses" ^
  -H "Content-Type: application/json" ^
  -d "{\"code\":\"S3X\",\"title\":\"S3\",\"description\":\"test\"}"
```

```text
HTTP/1.1 401

{"timestamp":1793014152.705124700,"status":401,"error":"Unauthorized","message":"Authentication required","path":"/api/courses","fieldErrors":null}
```

### 9. POST /api/auth/signup remains public - expect 201

```cmd
curl.exe -sS -i -X POST "http://localhost:8080/api/auth/signup" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"s3.public@test.com\",\"password\":\"password1\",\"fullName\":\"S3 Public\"}"
```

```text
HTTP/1.1 201

{"id":8,"email":"s3.public@test.com","fullName":"S3 Public","role":"STUDENT","createdAt":"2026-10-26T11:30:18.580653600Z"}
```

### S3 checks

- Public signup and login still work after the security chain change.
- Public GET `/api/courses` returns 200 with and without an invalid token.
- Protected GET `/api/users/me` returns 401 without a valid token.
- Malformed, tampered, and expired tokens all return 401.
- A valid token returns 200 with `UserResponse`.
- The response contains no `passwordHash`.
- POST `/api/courses` is not public and returns 401 without a token.
- The 401 response body is an `ApiError` JSON object.
- The filter does not write the 401 response. The entry point does.
