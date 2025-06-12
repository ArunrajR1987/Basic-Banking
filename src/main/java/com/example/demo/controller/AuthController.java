package com.example.demo.controller;

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

import com.example.demo.config.JWTUtil;
import com.example.demo.dto.AuthRequest;
import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;

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
                Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authrequest.getUsername(), authrequest.getPassword()));
                String token = jwtUtil.generateToken(auth.getName());
                return ResponseEntity.ok(Map.of("token", token));
        } catch(Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authrequest) {
        if(customerRepository.findByUsername(authrequest.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists da punda");
        }

        Customer newCustomer = new Customer();
        newCustomer.setUsername(authrequest.getUsername());
        newCustomer.setPassword(passwordEncoder.encode(authrequest.getPassword()));
        newCustomer.setRoles(List.of("ROLE_USER"));
        customerRepository.save(newCustomer);
        return ResponseEntity.ok("User registered successfully");
    }
}
