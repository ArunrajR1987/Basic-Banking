package com.example.demo.validation;

import com.example.demo.entity.Customer;
import com.example.demo.exception.TransactionValidationException;

public class KYCValidation implements ValidationStrategy {

    private static final double KYC_LIMIT = 10000.00; 
    @Override
    public void validate(Customer sender, double amount) {
        if (!sender.isKycVerified() && amount > KYC_LIMIT) {
            throw new TransactionValidationException(
                "KYC verification required for transactions above " + KYC_LIMIT
            );
        }
    }
    
}
