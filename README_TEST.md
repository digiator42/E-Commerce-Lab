# E-CommerceLab Test Documentation

## Test Strategy
- **Unit Tests**: Service layer logic, utilities, isolated components
- **Integration Tests**: Full stack flows, security, DB integrity
- **ISTQB Formal Tests**: Jira tickets, Excel traceability matrix

## Test Scope
### In Scope
- Authentication (login, register, JWT, sessions)
- Two Factor Authentication (TOTP, email 2FA)
- Product management (CRUD, pagination, filtering)
- Security (role-based access, unauthorized access)
- Database integrity (1 Active Profile each (PG, MySQL))

### Out of Scope
- Payment gateway (third party, Simulated)

## Test Types
| Type | Description |
|---|---|
| Functional | Features work as specified |
| Security | Auth, roles, JWT validation, OWASP TOP 10 |
| Negative | Invalid inputs, edge cases |
| Boundary | Min/Max values, empty fields |


## Coverage Summary
| Module | Unit | Integration | ISTQB |
|---|---|---|---|
| Authentication | ✅ | ✅ | 🔲 |
| 2FA | ✅ | ✅ | 🔲 |
| Products | ✅ | ✅ | 🔲 |
| Orders | ✅ | ✅ | 🔲 |
| Cart | ✅ | ✅ | 🔲 |
| Wishlist | ✅ | ✅ | 🔲 |
| Admin | ✅ | ✅ | 🔲 |

## Quick Links
- [ISTQB Test Plan](./docs/test-plan.md)
- [ISTQB Test Cases](./docs/test-cases.xlsx)
- [Traceability Matrix](./docs/traceability-matrix.md)
- [Bug Report](./docs/bug-report.xlsx)


## Framework Tests
- ### Unit Tests, Integration Tests, End-to-End (E2E) / System Tests
- ### Performance Comparison: MySQL vs. PostgreSQL
  - **Scenario**: High-concurrency load test on `productId` lookup.
  - **Observation**: MySQL profile encountered   `CannotCreateTransactionException` under sustained load.
  - **Root Cause**: Hikari Pool exhaustion. 58 threads timed out waiting for a   connection (Pool size: 10).
  - **Comparison**: PostgreSQL profile handled the same load without connection   timeouts, suggesting faster query execution or better default connection   handling.
  - **Fixes**: 
      - Increased `maximum-pool-size` to 30.

## Run tests
- ### Framework Tests
  - #### Run all tests using the default PostgreSQL profile
    > $env:SPRING_PROFILES_ACTIVE="test"; ./mvnw test

    #### Run tests with a MySql profile
    > $env:SPRING_PROFILES_ACTIVE="test,mysql-primary"; ./mvnw test

    #### Run a specific test class
    > ./mvnw test -Dtest=ClassTestName
    #### Or you can use PS script `run-tests.ps1`, you need to set all below attributes.
    ```bash
    $env:MYSQL_USERNAME = ""
    $env:MYSQL_PASSWORD = ""

    $env:POSTGRES_USERNAME = ""
    $env:POSTGRES_PASSWORD = ""

    $env:JWT_SECRET_KEY = "32bitkey"
    $env:REMEMBER_ME_KEY = "32bitkey"
    ```
- ## QC Automation Tests
  - ### Run
    > ./mvnw verify -P qc "-Dspring.profiles.active=test,postgres"
    #### OR 
    > ./mvnw verify -P qc "-Dspring.profiles.active=test,mysql-primary"



   

