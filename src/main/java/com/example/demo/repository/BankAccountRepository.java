package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Account;
import com.example.demo.entity.Customer;

public interface BankAccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomer(Customer customer);
}