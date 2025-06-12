package com.example.demo.notification;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.demo.entity.Transaction;

@Component
public class FraudDetectionObserver implements TransactionObserver{
    private static final double FRAUD_THRESHOLD = 10000.00;
    
    @Override
    public void update(Transaction transaction) {
        if (transaction.getAmount().abs().compareTo(BigDecimal.valueOf(FRAUD_THRESHOLD)) > 0) {
            System.out.println("Fraud alert: Large transaction detected!");
        }
    }

    @Override
    public String getObserverType() {
        return "FRAUD_DETECTION";
    }
}