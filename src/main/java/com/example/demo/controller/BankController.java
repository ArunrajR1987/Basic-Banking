package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.service.TransactionService;

//add login registration, JWT auth flow, Observer/Template patterns for transaction notifications or audit log

@RestController
@RequestMapping("/bank")
public class BankController {
    
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private CustomerRepository customerRepo;

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestParam Long senderId, @RequestParam Long receiverId, @RequestParam Double amount) {
        TransactionDTO dto = new TransactionDTO(senderId, receiverId, amount);
        return ResponseEntity.ok(transactionService.transfer(dto));
    }
    @GetMapping("/balance/{id}")
    public ResponseEntity<Double> getBalance(@PathVariable Long id) {
        Customer c = customerRepo.findById(id).orElseThrow();
        return ResponseEntity.ok(c.getBalance());
    }
}
