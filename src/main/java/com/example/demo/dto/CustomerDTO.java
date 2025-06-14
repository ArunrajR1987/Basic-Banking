package com.example.demo.dto;

import java.util.List;

public class CustomerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private boolean kycVerified;
    private List<String> roles;
    
    // Constructors
    public CustomerDTO() {}
    
    public CustomerDTO(Long id, String firstName, String lastName, String email, 
                      String username, boolean kycVerified, List<String> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.kycVerified = kycVerified;
        this.roles = roles;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean isKycVerified() {
        return kycVerified;
    }
    
    public void setKycVerified(boolean kycVerified) {
        this.kycVerified = kycVerified;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}