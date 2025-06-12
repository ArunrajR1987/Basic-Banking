package com.example.demo.accounts;

import java.math.BigDecimal;

public class CheckingAccount extends Account {
    private BigDecimal localOverdraftLimit;
    
    public CheckingAccount(double overdraftLimit) {
        this.localOverdraftLimit = BigDecimal.valueOf(overdraftLimit);
        setOverdraftLimit(this.localOverdraftLimit);
    }
    
    @Override
    public void deposit(double amount) {
        setBalance(getBalance().add(BigDecimal.valueOf(amount)));
    }
    
    @Override
    public void withdraw(double amount) {
        BigDecimal amountBD = BigDecimal.valueOf(amount);
        if (getBalance().add(getOverdraftLimit()).compareTo(amountBD) >= 0) {
            setBalance(getBalance().subtract(amountBD));
        } else {
            throw new RuntimeException("Exceeds overdraft limit");
        }
    }
}
