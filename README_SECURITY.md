## Security Architecture

### [ ] Improper Authorization (IDOR)
- **Logic**: Ensure users cannot access or modify resources (orders, profiles, cart items) belonging to other users by manipulating IDs in the URL or request body.
  - **Test Case**: Attempt to get invoice `/api/orders/{id}/download-invoice` using an ID belonging to a different user account.
  - **Result** Success user A got to download and get invoice details of user B (including senstive data like use B name/address).
  - **Ref** [VULN-001](./docs/security)

### [ ] Broken Access Control
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
    * **Ref**: [VULN-002](./docs/security)
    ---
- #### Security Test Case: JWT none Algorithm
    * **Logic**: Prevent "alg: none" attacks that bypass signature verification.
    * **Test Case**: Attempt to access protected resources by setting JWT header to `{"alg": "none"}`.
    * **Result**: **Passed**. The system strictly enforces keyed hashing algorithms (HS256), returning a `401 Unauthorized`.
    * **Ref**: [VULN-003](./docs/security)


### [ ] Broken Authentication

### [ ] Sensitive Data Exposure

### [ ] Security Misconfiguration

### [ ] Cross-Site Scripting (XSS)

### [ ] Insecure Deserialization

### [ ] Using Components with Known Vulnerabilities

### [ ] Insufficient Logging & Monitoring


## Security Hardening 

- ### Content Security Policy (CSP)

    * **Observation**: `Content-Security-Policy` is missing.
    * **Risk**: **Medium**. Without CSP, the application relies solely on server-side input sanitization. If an injection vulnerability occurs, the browser will execute malicious scripts by default.

- ### HTTP Strict Transport Security (HSTS)

    * **Observation**: `Strict-Transport-Security` key is missing.
    * **Risk**: **High**. If a user accesses the site via a public Wi-Fi, an attacker can downgrade the connection to plain HTTP and intercept JWT tokens (Man in the middle attack).
