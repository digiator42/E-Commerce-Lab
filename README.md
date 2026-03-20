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


## yaml sample
```yaml
spring:
  datasource:
    postgres:
      jdbc-url: jdbc:postgresql://localhost:5432/master_ecom
      username: username
      password: password
      driver-class-name: org.postgresql.Driver
    mysql:
      jdbc-url: jdbc:mysql://localhost:3306/master_ecom
      username: username
      password: password
      driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: false
        default_batch_fetch_size: 20
        
  mail:
    host: smtp.gmail.com
    port: 465
    username: ${MAIL_USERNAME:placeholder}
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
    jwt-key: ${JWT_SECRET_KEY:32-byte-key}
    remember-key: ${REMEMBER_ME_KEY:32-byte-key}
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
