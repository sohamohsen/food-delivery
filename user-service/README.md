# User Service

## 📌 Overview
User Service is responsible for managing user profile data in the Food Delivery system.

## 🧩 Responsibilities
- Create user profile
- Update basic user data
- Manage address and location
- Upload profile image
- Enforce profile status rules
- Get all profile data 

## 🔐 Authentication
All endpoints require JWT authentication.

Header:
Authorization: Bearer <JWT_TOKEN>

## 📡 API Endpoints

### Basic Profile
- PUT /user-service/profile/basic
- GET /user-service/profile/basic

### Address
- PUT /user-service/profile/address
- GET /user-service/profile/address

### Location (Restaurant only)
- PUT /user-service/profile/location

### Profile Image
- PUT /user-service/profile/image
- GET /user-service/profile/image

## 🗄️ Database
- MySQL
- Table: user_profiles

## 🛠️ Tech Stack
- Java 17
- Spring Boot
- Spring Security
- JPA / Hibernate
- MySQL
- AWS S3

## ▶️ Run Locally
```bash
mvn clean install
mvn spring-boot:run
