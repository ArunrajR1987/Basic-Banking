package com.example.demo.accounts;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Customer;

@Service
public class AccountFactory {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountFactory(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(String type, Customer customer, BigDecimal initialDeposit) {
        Account account = new Account();
        account.setType(AccountType.valueOf(type.toUpperCase()));
        account.setCustomer(customer);
        account.setBalance(initialDeposit);
        
        return accountRepository.save(account);
    }
}