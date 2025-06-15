# PITA Banking Application

A Spring Boot banking application demonstrating various design patterns and secure authentication.

## Application Flow Diagram

```
┌─────────────────┐                                  ┌─────────────────────────────────────┐
│                 │                                  │             Spring Boot             │
│  React Client   │                                  │            Backend Server           │
│                 │                                  │                                     │
└────────┬────────┘                                  └─────────────────┬───────────────────┘
         │                                                             │
         │  1. Request Public Key                                      │
         │  GET /api/security/public-key                               │
         │ ─────────────────────────────────────────────────────────► │
         │                                                             │
         │  2. Return RSA Public Key                                   │
         │ ◄───────────────────────────────────────────────────────── │
         │                                                             │
         │                                                             │
         │  3. User enters credentials                                 │
         │  ┌─────────────────────┐                                    │
         │  │ Email: user@mail.com│                                    │
         │  │ Password: secret123 │                                    │
         │  └─────────────────────┘                                    │
         │                                                             │
         │  4. Encrypt password with public key                        │
         │  ┌─────────────────────┐                                    │
         │  │ Email: user@mail.com│                                    │
         │  │ Password: [ENCRYPTED]│                                   │
         │  └─────────────────────┘                                    │
         │                                                             │
         │  5. Send login/register request                             │
         │  POST /api/auth/login or /api/auth/register                 │
         │ ─────────────────────────────────────────────────────────► │
         │                                                             │
         │                                                             │
         │                                                             │  6. Decrypt password
         │                                                             │  ┌─────────────────┐
         │                                                             │  │PasswordSecurityUtil│
         │                                                             │  │.decryptPassword()│
         │                                                             │  └─────────────────┘
         │                                                             │
         │                                                             │  7. Authenticate user
         │                                                             │  ┌─────────────────┐
         │                                                             │  │AuthenticationManager│
         │                                                             │  │.authenticate()  │
         │                                                             │  └─────────────────┘
         │                                                             │
         │                                                             │  8. Generate JWT token
         │                                                             │  ┌─────────────────┐
         │                                                             │  │JWTUtil         │
         │                                                             │  │.generateToken()│
         │                                                             │  └─────────────────┘
         │                                                             │
         │  9. Return JWT token and user data                          │
         │ ◄───────────────────────────────────────────────────────── │
         │                                                             │
         │  10. Store token in local storage                           │
         │                                                             │
         │  11. Subsequent authenticated requests                      │
         │  GET /api/auth/me (with Authorization header)               │
         │ ─────────────────────────────────────────────────────────► │
         │                                                             │
         │                                                             │  12. Validate JWT token
         │                                                             │  ┌─────────────────┐
         │                                                             │  │JwtFilter       │
         │                                                             │  │.doFilterInternal()│
         │                                                             │  └─────────────────┘
         │                                                             │
         │                                                             │  13. Get user from DB
         │                                                             │  ┌─────────────────┐
         │                                                             │  │CustomerRepository│
         │                                                             │  │.findByUsername()│
         │                                                             │  └─────────────────┘
         │                                                             │
         │  14. Return user data                                       │
         │ ◄───────────────────────────────────────────────────────── │
         │                                                             │
```

### Data Flow

1. **Registration Flow**:
   - Client fetches public key from server
   - Password is encrypted client-side with RSA
   - Registration request sent with encrypted password
   - Server decrypts password using private key
   - Password is hashed with BCrypt before storage
   - User entity is saved to database
   - JWT token is generated and returned with user data

2. **Authentication Flow**:
   - Client fetches public key from server
   - Password is encrypted client-side with RSA
   - Login request sent with encrypted password
   - Server decrypts password using private key
   - Authentication manager validates credentials
   - JWT token is generated and returned with user data

3. **Protected Resource Access**:
   - Client includes JWT token in Authorization header
   - JwtFilter validates token and sets security context
   - Controller methods check authentication status
   - Data is returned based on user's permissions

### Object Transformation

```
┌───────────────┐      ┌───────────────┐      ┌───────────────┐
│  DTO Objects  │ ──► │ Entity Objects │ ──► │ Database Tables│
└───────────────┘      └───────────────┘      └───────────────┘
      ▲                      │                       │
      │                      │                       │
      └──────────────────────┘                       │
           CustomerMapper                            │
                                                     │
┌───────────────┐                                    │
│ JSON Response │ ◄────────────────────────────────────
└───────────────┘
```

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
| SpringDoc OpenAPI | 2.3.0 | API documentation with Swagger UI |

## Secure Password Handling

The application implements secure password handling to prevent exposure during transmission:

### Implementation

- **Client-Side Encryption**: Passwords are encrypted using RSA before transmission
- **Server-Side Decryption**: The server decrypts passwords using a private key
- **Public Key Endpoint**: `/api/security/public-key` provides the public key to clients
- **Automatic Detection**: The system automatically detects and handles encrypted passwords
- **BCrypt Hashing**: Passwords are still hashed with BCrypt before database storage

### Benefits

- **Protection in Transit**: Passwords remain secure even if network traffic is intercepted
- **Defense in Depth**: Adds security layer beyond HTTPS
- **Log Protection**: Prevents password exposure in logs and error messages
- **Secure Storage**: Maintains best practices for password storage
- **Transparent Integration**: Works seamlessly with existing authentication flows

For detailed implementation, see [README-secure-password.md](README-secure-password.md)

## API Documentation

The application includes Swagger UI for API documentation and testing.

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Swagger Configuration

The API documentation is configured using SpringDoc OpenAPI:

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info()
                        .title("PITA Banking API")
                        .description("REST API for PITA Banking Application")
                        .version("1.0")
                        .contact(new Contact()
                                .name("PITA Banking Team")
                                .email("support@pitabanking.com")
                                .url("https://pitabanking.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
```

### API Annotations

Controllers and DTOs are annotated with Swagger annotations for comprehensive documentation:

```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {

    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful authentication", 
                     content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authrequest) {
        // Implementation
    }
}
```

### Security Configuration

Swagger UI endpoints are permitted in the security configuration:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/swagger-ui/**").permitAll()
    .requestMatchers("/v3/api-docs/**").permitAll()
    .requestMatchers("/api-docs/**").permitAll()
    // Other configurations
)
```