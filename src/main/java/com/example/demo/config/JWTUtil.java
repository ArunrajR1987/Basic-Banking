package com.example.demo.config;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTUtil {
    
    private String secret;

    private long expiration;

    public JWTUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiratioString) {
        this.secret = secret;
        this.expiration = expiratioString;
    }

    public String generateToken(String username) {

        return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512).compact();
    }

    public String extracUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
    }

    public boolean validateToken(String token, String username) {
        return (extracUsername(token).equals(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
        .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getExpiration()
        .before(new Date());
    }
}