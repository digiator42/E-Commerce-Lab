## Security Architecture

## All security testing is done either manually or by automated Powershell scripting [here](./security-tests/init-test.ps1)

### [ X ] Improper Authorization (IDOR)
- **Logic**: Ensure users cannot access or modify resources (orders, profiles, cart items) belonging to other users by manipulating IDs in the URL or request body.
  - **Test Case**: Attempt to get invoice `/api/orders/{id}/download-invoice` using an ID belonging to a different user account.
  - **Result** Success user A got to download and get invoice details of user B (including senstive data like use B name/address).
  - **Ref** [VULN-001](./docs/tests/security-tests.xlsx)

### [ ✓ ] Broken Access Control
- #### Security Test Case: JWT Integrity & RBAC

    * **Logic**: Enforce strict RBAC (Role-Based Access Control) for administrative functions.
    * **Test Case**: Attempt to access `/api/admin/**` endpoints using a `ROLE_USER` token.
    * **Test Data**: Modified the claims, role to be `ROLE_ADMIN` instead of `ROLE_USER` to test signature validation.
        ```json    
        {
            "sub": "hassan@example.com",
            "role": "ROLE_ADMIN",
            "iat": 1775519224,
            "exp": 1775605624
        }
        ```
    * **Result**: **Passed**. A `401 Unauthorized` code was returned, confirming the server rejected the tampered signature.
    * **Ref**: [VULN-002](./docs/tests/security-tests.xlsx)
    ---
- #### Security Test Case: JWT none Algorithm
    * **Logic**: Prevent "alg: none" attacks that bypass signature verification.
    * **Test Case**: Attempt to access protected resources by setting JWT header to `{"alg": "none"}`.
    * **Result**: **Passed**. The system strictly enforces keyed hashing algorithms (HS256), returning a `401 Unauthorized`.
    * **Ref**: [VULN-003](./docs/tests/security-tests.xlsx)


### [ X ] Broken Authentication
    
- ####  Brute Force
    * **Logic**: Prevent automated password guessing by implementing account lockout or rate limiting.
    * **Test Case**: Attempt 5+ failed logins with the same username.
    * **Expected Result**: The account is temporarily locked, or a CAPTCHA is triggered.
    * **Result**: Currently, the system allows unlimited login attempts, making it vulnerable to credential stuffing.
    * **Ref**: [VULN-004](./docs/tests/security-tests.xlsx)

---

### [ X ] Sensitive Data Exposure
- #### Stack trace leak
    * **Test Case**: Send a `GET` request to `/api/cart/add/{id}` (which expects `POST`).
    * **Result**: **FAILED**. The system returned a `405 Method Error` with a full stack trace revealing package structures (`com.ecommerce`) and framework versions.
    * **Ref**: [VULN-005](./docs/tests/security-tests.xlsx)


### [ X ] Security Misconfiguration

- ### Content Security Policy (CSP)

    * **Observation**: `Content-Security-Policy` is missing.
    * **Risk**: **Medium**. Without CSP, the application relies solely on server-side input sanitization. If an injection vulnerability occurs, the browser will execute malicious scripts by default.

- ### HTTP Strict Transport Security (HSTS)

    * **Observation**: `Strict-Transport-Security` key is missing.
    * **Risk**: **High**. If a user accesses the site via a public Wi-Fi, an attacker can downgrade the connection to plain HTTP and intercept JWT tokens (Man in the middle attack).


### [ X ] Cross-Site Scripting (XSS)
- #### Stored XSS in Product Reviews
    * **Test Case**: Submit a product review with the payload: `<script><img title="</script><img src onerror=alert(1)>"></script>`.
    * **Result**: **FAILED**. The payload was stored in the database and executed in the browser of any user viewing the product page, triggering an alert box.
    * **Ref**: [VULN-006](./docs/tests/security-tests.xlsx)

### [ ✓ ] Insecure Deserialization

### [ ✓ ] Using Components with Known Vulnerabilities

### [ X ] Insufficient Logging & Monitoring

- #### Missing
    * Critical actions such as "Role Change" or "Product Price Update" are not logged with the performing admin's ID.
    * Invalid login attempts, unauthorized access to `/api/admin` are not logged
    * Ref: [VULN-007](./docs/tests/security-tests.xlsx)





