# Master Store Lab: Backend & Frontend Architecture

This lab documents the core business logic and architectural patterns used to build the e-commerce engine.

---

## Core Technical Stack

- [✓] **Backend**: Spring Boot  
- [✓] **Frontend**: Vanilla JavaScript (SPA), Tailwind CSS  
- [✓] **Database**: PostgreSQL

---

## Business Logic

- [✓] **User Management**
  - Features:  
    - [✓] Update display name  
    - [✓] Update shipping address  
    - [✓] Change password  
    - [✓] Enable 2FA  

- [✓] **Image & Asset Management**  
  - Pattern: Virtual Path Mapping, Files are saved to `uploads/`.  

- [✓] **Cart Persistence & Sync**  

- [✓] **Transactional Checkout (Snapshot Pattern)**  
  - Pattern: Order Snapshots, copies product name and price into `OrderItem`.  

- [✓] **Payment Gateway Simulation**  
- [x] Offers / Discounts / Gift Cards Logic



## Layered Structure

- [✓] **Model Layer (Domain)**: JPA Entities (`User`, `Product`, `CartItem`, `Order`, `Review`) defining schema and relationships  
- [✓] **Repository Layer**: Spring Data JPA interfaces + custom JPQL for optimized fetching  
- [✓] **Service Layer**: Business rules (e.g., attributes validation, price snapshotting)  
- [✓] **Controller Layer (API)**: RESTful endpoints


## Data Transfer Pattern (DTOs)

- [✓] Prevents entity leaking by decoupling DB schema from frontend  
- [✓] Encapsulation


## Global Exception Handling

- [✓] **Global Exception Handler**:  
  > Uses `@ControllerAdvice` to catch and format backend errors into JSON for `apiFetch`  

- [✓] **SPA Routing Support**:  
  > Logic: Catch-all fallback returns `index.html`, Frontend router handles route rendering  
