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
- ### Unit Tests
    Isolate specific components or layers, often mocking out dependencies (like services or repositories) to test business logic and input validation quickly.

- ### Integration Tests
    Verify that different parts of the application work together correctly.

- ### End-to-End (E2E) / System Tests
    Complete application tests that simulate real user interactions, including authentication, product browsing, cart management, and checkout processes.