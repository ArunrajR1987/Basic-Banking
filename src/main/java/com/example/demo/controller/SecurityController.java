package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.security.PasswordSecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for security-related endpoints.
 */
@RestController
@RequestMapping("/api/security")
@Tag(name = "Security", description = "Security-related API endpoints")
public class SecurityController {

    @Autowired
    private PasswordSecurityUtil passwordSecurityUtil;
    
    /**
     * Endpoint to get the public key for password encryption.
     * The client should use this key to encrypt passwords before sending them to the server.
     * 
     * @return A map containing the public key as a Base64 encoded string
     */
    @Operation(summary = "Get public key", 
              description = "Returns the public key for password encryption on the client side")
    @GetMapping("/public-key")
    public Map<String, String> getPublicKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", passwordSecurityUtil.getPublicKeyBase64());
        return response;
    }
}