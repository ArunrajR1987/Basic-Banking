package com.example.demo.mapper;

import com.example.demo.dto.CustomerDTO;
import com.example.demo.entity.Customer;

public class CustomerMapper {
    
    public static CustomerDTO toDTO(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        return new CustomerDTO(
            customer.getId(),
            customer.getFirstName(),
            customer.getLastName(),
            customer.getEmail(),
            customer.getUsername(),
            customer.isKycVerified(),
            customer.getRoles()
        );
    }
}