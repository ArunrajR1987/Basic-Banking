package com.example.demo.accounts_fees;


public class PremiumAccountFee implements FeeStrategy {
    @Override
    public double calculateFee(double amount) {
        return amount * 0.05;
    }
    
    @Override
    public String getFeeType() {
        return "PREMIUM";
    }
}