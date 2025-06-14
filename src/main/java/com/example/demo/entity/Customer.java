package com.example.demo.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;
    private boolean kycVerified;
    
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();
    
    @OneToMany(mappedBy = "customer")
    private List<BankAccount> accounts = new ArrayList<>();
    
    // Explicit getter for kycVerified to ensure it's available
    public boolean isKycVerified() {
        return kycVerified;
    }
    
    // Generate username from firstName and lastName
    public void generateUsername() {
        if (firstName != null && lastName != null) {
            this.username = (firstName.toLowerCase() + "." + lastName.toLowerCase()).replaceAll("\\s+", "");
        }
    }
}