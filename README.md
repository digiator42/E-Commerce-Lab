# E-Commerce-Lab

## Overview
E-Commerce-Lab serves as both a development playground and a testing lab.

## Features
- Product catalog management
- User account and role handling
- Shopping cart and checkout flow
- Admin dashboard with admin priviliges
- Payment simulation

refere to [README_DEV.md](README_DEV.md) for detailed technical architecture and business logic.

## Tech Stack
- **Backend:** Java, Spring Boot
- **Build Tool:** Maven
- **Testing:** Selenium, JUnit, Automation scripts
- **Database:** PostgreSQL, MySQL
- **Version Control:** GitHub

## Project Structure
```ps
.
├── src
│   ├── main
│   │   ├── java/com/ecommerce/lab
│   │   │   ├── config/             # Spring Security & App Configuration
│   │   │   ├── controller/         # REST Endpoints
│   │   │   ├── model/              # JPA Entities
│   │   │   ├── dto/                # Data Transfer Objects
│   │   │   ├── repository/         # Multi-DB (MySQL/Postgres) support
│   │   │   ├── security/           # JWT & Auth Logic
│   │   │   └── service/            # Business Logic
│   │   └── resources
│   │       ├── static/             # Vanilla JS, CSS, & Components
│   │       └── templates/          # HTML Views
│   │
│   └── test
│       ├── java/com/ecommerce/lab
│       │   ├── unit/               # Fast Development/Component Tests
│       │   └── automation/         # Heavy QC Automation (Selenium + TestNG)
│       │       ├── runners/        # Cucumber IT Runners
│       │       ├── steps/          # Gherkin Step Definitions
│       │       └── pages/          # Selenium Page Object Models (POM)
│       └── resources
│           └── features/           # Gherkin .feature files
│
├── docs/                           # Manual test cases & project documentation
├── security-tests/                 # OWASP Top 10 Audit logs & scripts
├── .github/workflows/              # CI/CD Pipeline (GitHub Actions)
└── pom.xml                         # Build config with QC & Dev profiles
```

## Getting Started
   ```bash
   > git clone https://github.com/digiator42/E-Commerce-Lab.git
   > cd E-Commerce-Lab
   > ./mvnw spring-boot:run # this would run with postgres
   # If you want to run with mysql db / windows
   > $env:SPRING_PROFILES_ACTIVE="mysql-primary"; ./mvnw spring-boot:run
   # linux
   > export SPRING_PROFILES_ACTIVE="mysql-primary" ./mvnw spring-boot:run

   ```
## Run using docker
```bash
# Build the image
docker build -t ecommerce-lab .

# Run the container
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=mysql-primary ecommerce-lab
```

## Testing
- The project follows a structured testing methodology combining automated frameworks and formal ISTQB practices.

  - **Manual & Formal Testing**: Comprehensive test suites are managed via **Jira (Sprint 4)** and documented in a detailed **Excel Traceability Matrix**. This includes functional verification, edge case analysis, and regression logs.
  - **Security Testing**: Security audits are performed using a mix of manual penetration testing and automated PowerShell scripts to identify vulnerabilities like IDOR, XSS, and Broken Access Control.
  - **Automated Testing**: The framework utilizes **JUnit** for unit/integration logic and **Selenium** for end-to-end user flow validation.

  For a detailed breakdown of the test strategy, coverage summary, and security vulnerability reports, refer to [README_TEST.md](README_TEST.md) and [README_SECURITY.md](README_SECURITY.md).
