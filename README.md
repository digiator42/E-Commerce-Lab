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
- **CI/CD:** Jenkins
- **Version Control:** GitHub

## Getting Started
1. Clone the repository:
   ```bash
   git clone https://github.com/digiator42/E-Commerce-Lab.git
   ```
## yaml sample to run the server
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  mail:
    host: smtp.gmail.com
    port: 465
    username: ${MAIL_USERNAME:placeholder@placeholder.com}
    password: ${MAIL_PASSWORD:placeholder}
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
          socketFactory:
            port: 465
            class: javax.net.ssl.SSLSocketFactory
            fallback: false
  
  security:
    jwt-key: ${JWT_SECRET_KEY:32-character-secret-key}
    remember-key: ${REMEMBER_ME_KEY:32-character-secret-key}
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID:placeholder}
            client-secret: ${GOOGLE_CLIENT_SECRET:placeholder}
            scope:
              - email
              - profile

```
