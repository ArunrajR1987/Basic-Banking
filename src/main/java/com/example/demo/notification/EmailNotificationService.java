package com.example.demo.notification;

import java.math.BigDecimal;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Transaction;

@Service
@Primary
public class EmailNotificationService implements TransactionObserver {

    

    @Override
    public void update(Transaction transaction) {
       String message = String.format("Transaction Alert: %s %.2f to %s",
            transaction.getAmount().compareTo(BigDecimal.ZERO) > 0 ? "Received" : "Sent", 
            Math.abs(transaction.getAmount().doubleValue()),
            transaction.getReceiver().getCustomer().getUsername());
        System.out.println("Email sent: " + message);
    }

    @Override
    public String getObserverType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getObserverType'");
    }
    
}
