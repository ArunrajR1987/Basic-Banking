package com.example.demo.accounts;

import java.math.BigDecimal;

public class SavingAccount extends Account{
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.02");
    
    @Override
    public void deposit(double amount) {
        BigDecimal amountBD = BigDecimal.valueOf(amount);
        BigDecimal interest = amountBD.multiply(INTEREST_RATE);
        setBalance(getBalance().add(amountBD).add(interest));
    }
    
    @Override
    public void withdraw(double amount) {
        BigDecimal amountBD = BigDecimal.valueOf(amount);
        if (getBalance().compareTo(amountBD) >= 0) {
            setBalance(getBalance().subtract(amountBD));
        } else {
            throw new RuntimeException("Insufficient funds");
        }
    }
}
