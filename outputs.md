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

{"accessToken":"<redacted-jwt>","tokenType":"Bearer","expiresIn":3600}
```

The access token is redacted in this file. Decoded header and claims:

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
