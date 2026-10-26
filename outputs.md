# API Test Outputs

## Slice 1 — Signup

### 1. Valid signup — expect 201

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

### 2. Duplicate email, case-insensitive — expect 409

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

### 3. Invalid input — expect 400 with field errors

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

### 4. Role escalation attempt — expect 201 with STUDENT

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
