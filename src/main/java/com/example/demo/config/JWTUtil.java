package com.example.demo.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

@Component
public class JWTUtil {
    
    @Value("${jwt.secret:defaultsecretkey}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public JWTUtil() {
        // Using field injection with default value
    }

    public String generateToken(String username) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // Ensure key is at least 256 bits (32 bytes) for HS512
        if (keyBytes.length < 64) {
            // Pad the key if it's too short
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }

        return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(Keys.hmacShaKeyFor(keyBytes), SignatureAlgorithm.HS512).compact();
    }

    public String extracUsername(String token) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(keyBytes))
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
    }

    public boolean validateToken(String token, String username) {
        return (extracUsername(token).equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            byte[] paddedKey = new byte[64];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        
        return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getExpiration()
        .before(new Date());
    }
}