# Spring Boot Banking Application

A demo banking application built with Spring Boot, Spring Security, and JPA.

## Features

- User authentication with JWT
- Account management
- Loan processing
- Transaction handling
- Role-based access control

## Technologies

- Java 17
- Spring Boot 3.1.0
- Spring Security
- Spring Data JPA
- H2 Database
- JWT Authentication
- Lombok

## Getting Started

### Prerequisites

- JDK 17 or later
- Maven 3.6+

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run `mvn spring-boot:run`
4. Access the application at `http://localhost:8080`

## API Endpoints

- `/api/auth/**` - Authentication endpoints
- `/admin` - Admin-only endpoints
- `/bank/**` - User endpoints for banking operations

## License

This project is licensed under the MIT License - see the LICENSE file for details.