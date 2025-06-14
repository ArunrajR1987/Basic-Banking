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
| SpringDoc OpenAPI | 2.3.0 | API documentation with Swagger UI |

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