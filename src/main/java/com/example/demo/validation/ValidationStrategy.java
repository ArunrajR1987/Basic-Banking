package com.example.demo.validation;

import com.example.demo.entity.Customer;
import com.example.demo.exception.TransactionValidationException;


//Strategy design pattern
public interface ValidationStrategy {

    void validate(Customer sender, double amount) throws TransactionValidationException;
    
}
