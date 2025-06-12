package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Transaction;
import com.example.demo.notification.TransactionObserver;

@Service
public class TransactionSubject {
    private final List<TransactionObserver> observers = new ArrayList<>();

    @Autowired
    public TransactionSubject(List<TransactionObserver> obs) {
        this.observers.addAll(obs);
    }

    public void notifyObservers(Transaction transaction) {
        for(TransactionObserver observer: observers) {
            observer.update(transaction);
        }
    }
}
