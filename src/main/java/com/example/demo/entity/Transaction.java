package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import com.example.demo.accounts.Account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Getter
@Setter
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id")
    private Account sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id")
    private Account receiver;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Transaction() {
    }

    public Transaction(long longValue, Object amount2, long senderAccountId, long receiverAccountId, String string) {
        //TODO Auto-generated constructor stub
    }

    public static TransactionBuilder builder() {
        return new TransactionBuilder();
    }

    public static class TransactionBuilder {
        private final Transaction transaction = new Transaction();

        public TransactionBuilder amount(BigDecimal amount) {
            transaction.setAmount(amount);
            return this;
        }

        public TransactionBuilder sender(Account sender) {
            transaction.setSender(sender);
            return this;
        }

        public TransactionBuilder receiver(Account receiver) {
            transaction.setReceiver(receiver);
            return this;
        }

        public Transaction build() {
            transaction.setStatus(TransactionStatus.COMMITTING);
            return transaction;
        }    }

    public Object getPreviousStatus() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPreviousStatus'");
    }
}