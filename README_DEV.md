# Master Store Lab: Backend & Frontend Architecture

This lab documents the core business logic and architectural patterns used to build the e-commerce engine.

---

## Core Technical Stack

- [✓] **Backend**: Spring Boot  
- [✓] **Frontend**: Vanilla JavaScript (SPA), Tailwind CSS  
- [✓] **Database**: PostgreSQL

---

## Business Logic

- [✓] **Authentication**
  - Hybrid JWT & Session-based Authentication
  - Logic:
    - **Hybrid Validation**: The system accepts both JWT and traditional Session cookies.
    - **JWT Flexibility**: For general testing, JWT is optional; however, it is strictly enforced for all `/api/admin/**` routes.
    - **Session Management**: If no JWT is provided in the request header, the security filter chain falls back to standard Session-based authentication.
    - **Extended Persistence**: Includes a "Remember Me" feature that extends both the session cookie and the JWT validity to a 14-day period.

- [✓] **User Management**
  - Features:  
    - [✓] Login / Register (Form / OAuth2 Google)
    - [✓] Update display name  
    - [✓] Update shipping address  
    - [✓] Change/Forget, Reset password
    - [✓] Enable/Disable 2FA
    - [✓] Store Balance management
    - [✓] Redeem service, history (REDEEM, PURCHASE)

- [✓] **Admin Management**
    - Admin Features
      - [✓] Inventory requests
      - [✓] Add/Edit/Delete products
      - [✓] Customer Orders requests
      - [✓] Users Access management (ROLE_ADMIN & ROLE_USER)
      - [✓] Self downgrade protection
      - [✓] Coupons in admin dashboard (add, edit, activate)


- [✓] **Role-Based Access Control (RBAC)**
  > Admin routes (/api/admin/**) are guarded by hasRole('ADMIN') and JWT token.

- [✓] **Image & Asset Management**  
  - Pattern: Virtual Path Mapping, Files are saved to `uploads/`.  

- [✓] **Cart Persistence & Sync**  

- [✓] **Wishlist Persistence & Sync**  

- [✓] **Transactional Checkout (Snapshot Pattern)**  
  - Pattern: Order Snapshots, copies product name and price into `OrderItem`.  

- [✓] **Payment Gateway Simulation**  

- [✓] **Email Service**
    - Features:  
    - [✓] Pass changes requests
    - [✓] 2FA

- [✓] **Invoice Service**


- [✓] **Offers / Discounts / Gift Cards Logic**
  - [✓] Coupons/Discounts applying
  - [✓] GiftCards as virtual products



## Layered Structure

- [✓] **Model Layer (Domain)**: JPA Entities (`User`, `Product`, `CartItem`, `Order`, `Review`) defining schema and relationships  
- [✓] **Repository Layer**: Spring Data JPA interfaces + custom JPQL for optimized fetching  
- [✓] **Service Layer**: Business rules (e.g., DTO validation, price snapshotting)  
- [✓] **Controller Layer (API)**: RESTful endpoints


## Data Transfer Pattern (DTOs)

- [✓] Prevents entity leaking by decoupling DB schema from frontend  
- [✓] Encapsulation


## Global Exception Handling

- [✓] **Global Exception Handler**:  
  > Uses `@ControllerAdvice` to catch and format backend errors into JSON

- [✓] **SPA Routing Support**:  
  > Logic: Catch-all fallback returns `index.html`, Frontend router handles route rendering  
