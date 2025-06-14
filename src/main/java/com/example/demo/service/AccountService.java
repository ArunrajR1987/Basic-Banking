package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Account;
import com.example.demo.entity.Customer;
import com.example.demo.repository.BankAccountRepository;

@Service
public class AccountService {
    
    @Autowired
    private BankAccountRepository accountRepository;
    
    public Account createAccount(Customer customer, String accountType, double initialBalance) {
        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountType(accountType);
        account.setBalance(initialBalance);
        account.setAccountNumber(generateAccountNumber());
        return accountRepository.save(account);
    }
    
    private String generateAccountNumber() {
        // Simple account number generation - in production use a more robust method
        return "ACC" + System.currentTimeMillis();
    }
}