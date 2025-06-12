package com.example.demo.context;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.Customer;
import com.example.demo.validation.ValidationStrategy;

public class TransactionValidationContext {
    private List<ValidationStrategy> validationStrategies;

    public TransactionValidationContext() {
        this.validationStrategies = new ArrayList<>();
    }

    public void addStrategy(ValidationStrategy strategy) {
        validationStrategies.add(strategy);
    }

    public void validateAll(Customer sender, double amount) {
        for(ValidationStrategy strategy: validationStrategies) {
            strategy.validate(sender, amount);
        }
    }
}
