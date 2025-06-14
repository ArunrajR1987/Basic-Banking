package com.example.demo.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Account;
import com.example.demo.entity.Customer;
import com.example.demo.exception.TransactionValidationException;
import com.example.demo.repository.BankAccountRepository;

@Component
public class BalanceValidation implements ValidationStrategy {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Override
    public void validate(Customer sender, double amount) {
        List<Account> accounts = bankAccountRepository.findByCustomer(sender);
        double totalBalance = accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
                
        if (totalBalance < amount) {
            throw new TransactionValidationException(
                "Insufficient funds. Available: " + totalBalance
            );
        }
    }
    
}