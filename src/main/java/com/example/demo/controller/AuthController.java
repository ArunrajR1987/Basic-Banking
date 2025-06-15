package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.JWTUtil;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.CustomerDTO;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Customer;
import com.example.demo.mapper.CustomerMapper;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.security.PasswordSecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private PasswordSecurityUtil passwordSecurityUtil;

    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful authentication", 
                     content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // Handle the password - decrypt it if it was encrypted on the client side
            String password = handleEncryptedPassword(authRequest.getPassword());
            if (password == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password format");
            }
            
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), password)
            );
            
            return createAuthenticationResponse(auth.getName());
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @Operation(summary = "Register user", description = "Creates a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        // Check if email is already in use
        if(customerRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        // Handle the password - decrypt it if it was encrypted on the client side
        String password = handleEncryptedPassword(registerRequest.getPassword());
        if (password == null) {
            return ResponseEntity.badRequest().body("Invalid password format");
        }
        
        Customer newCustomer = createCustomer(registerRequest, password);
        customerRepository.save(newCustomer);
        
        return createAuthenticationResponse(newCustomer.getUsername());
    }
    
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User information retrieved successfully", 
                     content = @Content(schema = @Schema(implementation = CustomerDTO.class))),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
        
        String username = authentication.getName();
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        CustomerDTO customerDTO = CustomerMapper.toDTO(customer);
        return ResponseEntity.ok(customerDTO);
    }
    
    /**
     * Handles potentially encrypted passwords
     * 
     * @param password The password that might be encrypted
     * @return The decrypted password or null if decryption failed
     */
    private String handleEncryptedPassword(String password) {
        if (passwordSecurityUtil.isEncrypted(password)) {
            try {
                return passwordSecurityUtil.decryptPassword(password);
            } catch (Exception e) {
                return null;
            }
        }
        return password;
    }
    
    /**
     * Creates a new Customer entity from registration data
     * 
     * @param registerRequest The registration request data
     * @param password The processed password (decrypted if needed)
     * @return A new Customer entity
     */
    private Customer createCustomer(RegisterRequest registerRequest, String password) {
        Customer newCustomer = new Customer();
        newCustomer.setFirstName(registerRequest.getFirstName());
        newCustomer.setLastName(registerRequest.getLastName());
        newCustomer.setEmail(registerRequest.getEmail());
        newCustomer.generateUsername(); // Generate username from first and last name
        newCustomer.setPassword(passwordEncoder.encode(password));
        newCustomer.setRoles(List.of("ROLE_USER"));
        return newCustomer;
    }
    
    /**
     * Creates the authentication response with token and user data
     * 
     * @param username The username to generate a token for
     * @return ResponseEntity with token and user data
     */
    private ResponseEntity<?> createAuthenticationResponse(String username) {
        String token = jwtUtil.generateToken(username);
        
        Customer customer = customerRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        CustomerDTO customerDTO = CustomerMapper.toDTO(customer);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", customerDTO);
        
        return ResponseEntity.ok(response);
    }
}