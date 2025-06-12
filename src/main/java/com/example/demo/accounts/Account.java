package com.example.demo.accounts;

import java.math.BigDecimal;

import com.example.demo.entity.Customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(precision = 15, scale = 2)
    private BigDecimal overdraftLimit = BigDecimal.ZERO;

    public boolean hasSufficientBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public boolean canWithdraw(BigDecimal amount) {
        return balance.add(overdraftLimit).compareTo(amount) >= 0;
    }
    
    public void deposit(double amount) {
        this.balance = this.balance.add(BigDecimal.valueOf(amount));
    }
    
    public void withdraw(double amount) {
        if (canWithdraw(BigDecimal.valueOf(amount))) {
            this.balance = this.balance.subtract(BigDecimal.valueOf(amount));
        } else {
            throw new RuntimeException("Insufficient funds");
        }
    }
}
