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
