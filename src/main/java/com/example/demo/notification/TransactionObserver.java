package com.example.demo.notification;

import com.example.demo.entity.Transaction;

public interface TransactionObserver {
    void update(Transaction transaction);
    String getObserverType();
}