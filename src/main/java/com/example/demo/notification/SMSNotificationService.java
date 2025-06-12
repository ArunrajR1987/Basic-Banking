package com.example.demo.notification;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Transaction;

@Service
public class SMSNotificationService implements TransactionObserver {

    @Override
    public void update(Transaction transaction) {
        System.out.println("SMS sent: Transaction of " + 
            transaction.getAmount() + " processed");
    }

    @Override
    public String getObserverType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObserverType'");
    }

    
    
}
