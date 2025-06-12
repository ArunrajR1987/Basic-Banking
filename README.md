# Pita Banking Application - Backend

Spring Boot backend for the Pita Banking Application.

## Project Structure

- Spring Boot REST API with JWT authentication
- JPA/Hibernate for database access
- Design patterns: Observer, Factory, Strategy, Template

## Features

- User authentication (login/register)
- Account management
- Transaction processing
- Fee calculation based on account types
- Transaction validation and notification

## Technologies

- Java 17
- Spring Boot
- Spring Security with JWT
- JPA/Hibernate
- Maven

## Getting Started

1. Clone the repository
2. Navigate to the project root directory
3. Run `./mvnw spring-boot:run` to start the Spring Boot application
4. The API will be available at `http://localhost:8080`

## API Endpoints

### Authentication
- POST `/api/auth/login` - Login with username and password
- POST `/api/auth/register` - Register a new user

### Banking
- GET `/bank/balance/{id}` - Get account balance
- POST `/bank/transfer` - Transfer funds between accounts

## Frontend Repository

The React frontend for this application is available in a separate repository.

## License

This project is licensed under the MIT License.