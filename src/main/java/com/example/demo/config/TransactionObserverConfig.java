package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.example.demo.notification.EmailNotificationService;
import com.example.demo.notification.FraudDetectionObserver;
import com.example.demo.notification.SMSNotificationService;
import com.example.demo.notification.TransactionObserver;
import com.example.demo.service.TransactionSubject;

import jakarta.annotation.PostConstruct;

@Configuration
public class TransactionObserverConfig {
    @Autowired
    private TransactionSubject transactionSubject;
    
    @Autowired
    private EmailNotificationService emailObserver;
    
    @Autowired
    private SMSNotificationService smsObserver;
    
    @Autowired
    private FraudDetectionObserver fraudObserver;

    @PostConstruct
    public void setupObservers() {
        List<TransactionObserver> testList = new ArrayList<>();
        testList.add(emailObserver);
        testList.add(smsObserver);
        testList.add(fraudObserver);
        transactionSubject = new TransactionSubject(testList);
    }
}
