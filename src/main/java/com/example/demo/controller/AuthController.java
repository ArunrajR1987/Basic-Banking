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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.PasswordSecurityUtil;

import com.example.demo.config.JWTUtil;
import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.CustomerDTO;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Customer;
import com.example.demo.mapper.CustomerMapper;
import com.example.demo.repository.CustomerRepository;

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

    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful authentication", 
                     content = @Content(schema = @Schema(implementation = Map.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authrequest) {
        try {
                // Handle the password - decrypt it if it was encrypted on the client side
                String password = authrequest.getPassword();
                if (passwordSecurityUtil.isEncrypted(password)) {
                    try {
                        password = passwordSecurityUtil.decryptPassword(password);
                    } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password format");
                    }
                }
                
                Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authrequest.getUsername(), password)
                );
                String token = jwtUtil.generateToken(auth.getName());
                
                // Get customer details but exclude password
                Customer customer = customerRepository.findByUsername(auth.getName()).orElseThrow();
                CustomerDTO customerDTO = CustomerMapper.toDTO(customer);
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", customerDTO);
                
                return ResponseEntity.ok(response);
        } catch(Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @Autowired
    private PasswordSecurityUtil passwordSecurityUtil;

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

        Customer newCustomer = new Customer();
        newCustomer.setFirstName(registerRequest.getFirstName());
        newCustomer.setLastName(registerRequest.getLastName());
        newCustomer.setEmail(registerRequest.getEmail());
        newCustomer.generateUsername(); // Generate username from first and last name
        
        // Handle the password - decrypt it if it was encrypted on the client side
        String password = registerRequest.getPassword();
        if (passwordSecurityUtil.isEncrypted(password)) {
            try {
                password = passwordSecurityUtil.decryptPassword(password);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid password format");
            }
        }
        
        // Encode the password before storing
        newCustomer.setPassword(passwordEncoder.encode(password));
        newCustomer.setRoles(List.of("ROLE_USER"));
        customerRepository.save(newCustomer);
        
        // Generate token for the newly registered user
        String token = jwtUtil.generateToken(newCustomer.getUsername());
        
        // Convert to DTO to avoid exposing password
        CustomerDTO customerDTO = CustomerMapper.toDTO(newCustomer);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", customerDTO);
        
        return ResponseEntity.ok(response);
    }
}