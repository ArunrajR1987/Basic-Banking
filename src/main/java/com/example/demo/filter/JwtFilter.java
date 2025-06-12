package com.example.demo.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

              String authHeader = request.getHeader("Authorization");
              String username = null;
              String token = null;

              if(authHeader != null && authHeader.startsWith("Bearer ")) {
                 token = authHeader.substring(7);
                 username = jwtUtil.extracUsername(token);
              }

              if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                 UserDetails userdetails = customDetailsService.loadUserByUsername(username);
                 if(jwtUtil.validateToken(token, username)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userdetails, null, userdetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            }
            filterChain.doFilter(request, response);
        }
    }
