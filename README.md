# PITA Banking Application

A Spring Boot banking application demonstrating various design patterns and secure authentication.

## Libraries and Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Spring Boot | 3.2.3 | Core framework providing auto-configuration, dependency injection, and embedded server |
| Spring Data JPA | 3.2.3 | Simplifies data access layer implementation with repository abstraction |
| Spring Web | 3.2.3 | Provides web-related features including RESTful API support |
| Spring Security | 3.2.3 | Authentication and authorization framework for securing the application |
| MySQL Connector | Runtime | Database connectivity for production environment |
| H2 Database | Runtime | In-memory database for development and testing |
| JJWT | 0.11.5 | JSON Web Token implementation for stateless authentication |
| Lombok | 1.18.30 | Reduces boilerplate code through annotations |

## Application Architecture

The application follows a layered architecture with the following components:

1. **Controllers**: Handle HTTP requests and delegate to services
2. **Services**: Implement business logic and orchestrate operations
3. **Repositories**: Provide data access abstraction
4. **Entities**: Represent database tables and domain objects
5. **DTOs**: Transfer data between layers
6. **Filters**: Process HTTP requests/responses for cross-cutting concerns

## Design Patterns Implemented

- **Factory Pattern**: `AccountFactory` creates different account types
- **Strategy Pattern**: `FeeStrategy` for different fee calculation strategies
- **Observer Pattern**: `TransactionObserver` for notification on transactions
- **Template Method**: `LoanProcessingTemplate` defines skeleton for loan processing

## Spring Security Implementation

### Overview

The application implements a JWT-based authentication system using Spring Security. The security configuration is currently modified to disable auto-configuration (`SecurityAutoConfiguration.class`) to resolve bean creation issues during development.

### Key Components

1. **SecurityConfig**: Central configuration class for Spring Security
   - Configures HTTP security, CORS, CSRF, and authorization rules
   - Defines security filter chain and authentication entry points
   - Sets up stateless session management (no server-side sessions)
   - Configures password encoding with BCrypt

2. **JwtFilter**: Custom filter extending `OncePerRequestFilter`
   - Intercepts incoming requests to validate JWT tokens
   - Extracts user information from tokens
   - Sets up Spring Security context for authenticated users
   - Handles authentication failures with proper HTTP responses
   - Excludes specific paths from authentication requirements

3. **JWTUtil**: Utility class for JWT operations
   - Generates tokens upon successful authentication
   - Validates token integrity and expiration
   - Extracts username and other claims from tokens
   - Implements secure signing with HS512 algorithm
   - Handles token parsing exceptions gracefully

4. **CustomerDetailsService**: Implements Spring's `UserDetailsService`
   - Loads user details from the database for authentication
   - Converts application-specific user model to Spring Security's `UserDetails`
   - Maps user roles to Spring Security authorities

### Authentication Flow

1. Client submits credentials to `/api/auth/login`
2. `AuthController` validates credentials using Spring's `AuthenticationManager`
3. Upon successful authentication, `JWTUtil` generates a signed JWT token
4. Token is returned to client for storage (typically in local storage or cookies)
5. Client includes token in Authorization header for subsequent requests
6. `JwtFilter` validates token and establishes security context if valid
7. Protected resources check authorities before granting access

### Security Measures

- **Password Encryption**: BCrypt hashing for secure password storage
- **Token Signing**: HS512 algorithm with secure key management
- **CSRF Protection**: Disabled for stateless API (relies on JWT security)
- **CORS Configuration**: Configured to allow specific origins and methods
- **Stateless Architecture**: No server-side session state, improving scalability
- **Path-Based Security**: Different security rules for different API paths
- **Custom Error Handling**: JSON responses for authentication failures

### Public Endpoints

The following endpoints are accessible without authentication:
- `/api/auth/login` - For user login
- `/api/auth/register` - For user registration
- `/api/bank/accounts` - Public endpoint for viewing accounts
- All OPTIONS requests (for CORS preflight)

### Protected Endpoints

All other endpoints require a valid JWT token in the Authorization header:
- `/api/bank/balance/{id}` - For checking account balance
- `/api/bank/transfer` - For transferring funds between accounts

## Authentication Flow

1. User registers via `/api/auth/register` endpoint
2. User logs in via `/api/auth/login` endpoint and receives JWT token
3. JWT token is included in subsequent requests as Bearer token
4. `JwtFilter` validates token and sets authentication context
5. Protected endpoints check user roles and permissions

## Transaction Flow

1. Client initiates transaction via `/api/bank/transfer` endpoint
2. `TransactionService` validates transaction using strategy pattern
3. Transaction is processed and persisted
4. Observer pattern notifies relevant services (email, SMS, audit)
5. Response is returned to client

## Account Management

- Different account types (Checking, Saving) inherit from base `Account` class
- Each account type has specific fee structure via strategy pattern
- Account operations are secured based on user roles

## Configuration Properties

### Application Properties

```properties
# Application Identification
spring.application.name=pita
# Defines the application name for service discovery and logging
# Value 'pita' identifies this specific application instance

# Database Connection Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/pita_db
# JDBC URL for MySQL database connection
# Points to a local MySQL instance on default port 3306 with database name 'pita_db'

spring.datasource.username=root
# Database username for authentication
# Using 'root' provides full database privileges (consider using a restricted user in production)

spring.datasource.password=Kaavya_0206
# Database password for authentication
# Custom password for the database user

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
# Controls database schema generation
# 'update' value automatically updates schema based on entity changes without data loss

spring.jpa.show-sql=true
# Controls SQL logging
# 'true' logs all SQL statements to console for debugging and monitoring

spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
# Specifies the SQL dialect for the database
# MySQL8Dialect optimizes SQL generation for MySQL 8.x features

# JWT Configuration
jwt.secret=${JWT_SECRET:defaultsecretkey}
# Secret key for JWT token signing and verification
# Uses environment variable JWT_SECRET if available, otherwise falls back to 'defaultsecretkey'

jwt.expiration=3600000
# JWT token validity period in milliseconds
# 3600000ms = 1 hour before tokens expire and require re-authentication

# Server Configuration
server.port=8080
# HTTP port for the application server
# Standard development port, accessible via http://localhost:8080

# Error Handling Configuration
server.error.whitelabel.enabled=false
# Controls default error page
# 'false' disables Spring's default error page to prevent redirect loops

spring.web.resources.add-mappings=false
# Controls static resource handling
# 'false' disables automatic mapping of resources to prevent conflicts with API paths

# Logging Configuration
logging.level.root=INFO
# Base logging level for all packages
# 'INFO' provides standard operational logging without excessive detail

logging.level.com.example.demo=INFO
# Application-specific logging level
# 'INFO' captures important application events without debug details

logging.level.org.springframework.security=INFO
# Security framework logging level
# 'INFO' logs authentication events and security decisions
```

## Getting Started

1. Configure database in `application.properties`
2. Run the application using Maven: `mvn spring:run`
3. Access the API at `http://localhost:8080`

## API Endpoints

- **Authentication**
  - POST `/api/auth/register` - Register new user
  - POST `/api/auth/login` - Login and get JWT token

- **Banking Operations**
  - GET `/api/bank/accounts` - View accounts (public endpoint)
  - GET `/api/bank/balance/{id}` - Check account balance
  - POST `/api/bank/transfer` - Transfer funds between accounts