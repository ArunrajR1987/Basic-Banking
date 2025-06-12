package com.example.demo.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import com.example.demo.config.JWTUtil;
import com.example.demo.service.CustomerDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter{

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private CustomerDetailsService customDetailsService;
    
    private final List<String> excludedPaths = Arrays.asList(
        "/api/auth/login", 
        "/api/auth/register"
    );
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        
        // Skip JWT validation for OPTIONS requests and excluded paths
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        return excludedPaths.stream()
            .anyMatch(p -> pathMatcher.match(p, path));
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            String username = null;
            String token = null;

            if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                if (StringUtils.hasText(token)) {
                    username = jwtUtil.extracUsername(token);
                }
            }

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userdetails = customDetailsService.loadUserByUsername(username);
                if(jwtUtil.validateToken(token, username)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userdetails, null, userdetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log exception but don't prevent the filter chain from continuing
            logger.error("Cannot set user authentication: {}", e);
        }
        
        filterChain.doFilter(request, response);
    }
}
