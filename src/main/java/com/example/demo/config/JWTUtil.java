package com.example.demo.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    public String extracUsername(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        
        return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(getSigningKey()))
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
    }

    public boolean validateToken(String token, String username) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(username)) {
            return false;
        }
        
        String extractedUsername = extracUsername(token);
        return (extractedUsername != null && extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        if (!StringUtils.hasText(token)) {
            return true;
        }
        
        return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(getSigningKey()))
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getExpiration()
        .before(new Date());
    }
}
}