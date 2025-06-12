package com.example.demo.entity;

import com.example.demo.accounts.Account;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "amount")
    private BigDecimal amount;
    
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Account sender;
    
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Account receiver;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TransactionStatus status;
    
    @Transient
    private TransactionStatus oldStatus;
    
    public Transaction(BigDecimal amount, Account sender, Account receiver, TransactionStatus status) {
        this.amount = amount;
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
    }
    
    // Custom setter to track old status
    public void setStatus(TransactionStatus status) {
        this.oldStatus = this.status;
        this.status = status;
    }
}
