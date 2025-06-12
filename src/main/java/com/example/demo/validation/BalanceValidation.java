package com.example.demo.validation;

import com.example.demo.entity.Customer;
import com.example.demo.exception.TransactionValidationException;

public class BalanceValidation implements ValidationStrategy {

    @Override
    public void validate(Customer sender, double amount) {
        if (sender.getBalance() < amount) {
            throw new TransactionValidationException(
                "Insufficient funds. Available: " + sender.getBalance()
            );
        }
    }
    
}
