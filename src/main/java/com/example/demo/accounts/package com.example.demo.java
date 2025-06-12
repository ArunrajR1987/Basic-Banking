package com.example.demo.accounts;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
package com.example.demo.accounts;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountFactory {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountFactory(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public Account createAccount(String type, Long customerId, BigDecimal initialDeposit) {
        Account account;
        switch (type.toUpperCase()) {
            case "SAVINGS":
                account = new SavingAccount();
                break;
            case "CHECKING":
                account = new CheckingAccount(0.0); // Assuming a default overdraft limit for checking
                break;
            default:
                throw new IllegalArgumentException("Invalid account type");
        }
        account.setCustomer(accountRepository.findCustomerById(customerId).orElseThrow(() -> new IllegalArgumentException("Customer not found")));
        account.setType(AccountType.valueOf(type.toUpperCase()));
        account.setBalance(initialDeposit);


        accountRepository.save(account);
        return account;

    }
}