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

```java
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class PitaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PitaApplication.class, args);
    }
}
```

### Key Components

#### 1. SecurityConfig

The `SecurityConfig` class configures Spring Security with JWT authentication:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/bank/accounts").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().permitAll())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
            })
        )
        .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Code Explanation:**
- `@EnableWebSecurity` activates Spring Security's web security support
- `SecurityFilterChain` defines the security filter chain with various configurations:
  - CORS configuration allows cross-origin requests from specified origins
  - CSRF protection is disabled for stateless REST APIs
  - URL-based authorization rules define which endpoints are public vs. protected
  - Stateless session management ensures no server-side session state
  - Custom authentication entry point returns JSON responses for auth failures
- `PasswordEncoder` bean configures BCrypt for password hashing
- `AuthenticationManager` bean is used for authenticating user credentials

#### 2. JwtFilter

The `JwtFilter` class intercepts requests to validate JWT tokens:

```java
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private CustomerDetailsService customDetailsService;
    
    private final List<String> excludedPaths = Arrays.asList(
        "/api/auth/login", 
        "/api/auth/register",
        "/error",
        "/api/bank/accounts"
    );
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        
        // Skip JWT validation for OPTIONS requests and excluded paths
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        for (String pattern : excludedPaths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // If path should be excluded, just continue the chain
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // For protected paths
        String authHeader = request.getHeader("Authorization");
        
        // No auth header for protected path - return 401 immediately
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
            return;
        }
        
        try {
            // Process token
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            
            if (username == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid token\"}");
                return;
            }
            
            // Set authentication
            UserDetails userdetails = customDetailsService.loadUserByUsername(username);
            if (jwtUtil.validateToken(token, username)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userdetails, null, userdetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Token validation failed\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
```

**Code Explanation:**
- `OncePerRequestFilter` ensures the filter is only executed once per request
- `shouldNotFilter` method determines which paths should bypass JWT validation
- `doFilterInternal` contains the main token validation logic:
  - Checks for the presence of an Authorization header with Bearer token
  - Extracts and validates the JWT token
  - Loads user details from the database
  - Sets up the Spring Security context with user authentication
  - Returns appropriate error responses for authentication failures

#### 3. JWTUtil

The `JWTUtil` class handles JWT token generation and validation:

```java
@Component
public class JWTUtil {
    
    @Value("${jwt.secret:defaultsecretkey}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private byte[] getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // Ensure key is at least 256 bits (32 bytes) for HS512
        if (keyBytes.length < 64) {
            // Pad the key if it's too short
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        return keyBytes;
    }

    public String generateToken(String username) {
        return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(Keys.hmacShaKeyFor(getSigningKey()), SignatureAlgorithm.HS512).compact();
    }

    public String extractUsername(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        
        try {
            return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(getSigningKey()))
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
        } catch (Exception e) {
            System.err.println("Error parsing JWT token: " + e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token, String username) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(username)) {
            return false;
        }
        
        String extractedUsername = extractUsername(token);
        return (extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        if (!StringUtils.hasText(token)) {
            return true;
        }
        
        try {
            return Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(getSigningKey()))
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration()
            .before(new Date());
        } catch (Exception e) {
            System.err.println("Error checking token expiration: " + e.getMessage());
            return true;
        }
    }
}
```

**Code Explanation:**
- `@Value` annotations inject properties from application.properties
- `getSigningKey()` ensures the signing key meets security requirements
- `generateToken()` creates a new JWT token with:
  - Username as the subject
  - Current time as issuance date
  - Expiration time based on configured duration
  - HS512 signature algorithm for security
- `extractUsername()` parses the token to extract the username
- `validateToken()` verifies token validity by checking:
  - Username matches the one in the token
  - Token has not expired
- `isTokenExpired()` checks if the token's expiration date has passed

#### 4. CustomerDetailsService

The `CustomerDetailsService` class loads user details for authentication:

```java
@Service
public class CustomerDetailsService implements UserDetailsService {
    
    @Autowired
    private CustomerRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer user = userRepo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
        
        List<GrantedAuthority> grantedAuthorities = user.getRoles().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), 
            user.getPassword(), 
            grantedAuthorities
        );
    }
}
```

**Code Explanation:**
- Implements Spring Security's `UserDetailsService` interface
- `loadUserByUsername()` method:
  - Retrieves user from database by username
  - Converts application-specific roles to Spring Security authorities
  - Returns a Spring Security User object with username, password, and authorities

#### 5. AuthController

The `AuthController` handles user authentication and registration:

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authrequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authrequest.getUsername(), 
                    authrequest.getPassword()
                )
            );
            String token = jwtUtil.generateToken(auth.getName());
            return ResponseEntity.ok(Map.of("token", token));
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authrequest) {
        if(customerRepository.findByUsername(authrequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        Customer newCustomer = new Customer();
        newCustomer.setUsername(authrequest.getUsername());
        newCustomer.setPassword(passwordEncoder.encode(authrequest.getPassword()));
        newCustomer.setRoles(List.of("ROLE_USER"));
        customerRepository.save(newCustomer);
        return ResponseEntity.ok("User registered successfully");
    }
}
```

**Code Explanation:**
- `@RestController` indicates this class handles REST API requests
- `/login` endpoint:
  - Authenticates user credentials using Spring's AuthenticationManager
  - Generates JWT token for authenticated users
  - Returns token in response or error for invalid credentials
- `/register` endpoint:
  - Checks if username already exists
  - Encodes password using BCrypt
  - Assigns default user role
  - Saves new user to database

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