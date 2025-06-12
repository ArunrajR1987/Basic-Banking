package com.example.demo.accounts_fees;


public interface FeeStrategy {
    double calculateFee(double amount);
    String getFeeType();
}
