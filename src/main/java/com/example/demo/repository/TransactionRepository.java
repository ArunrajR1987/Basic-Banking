package com.example.demo.repository;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.sender.id = :accountId OR t.receiver.id = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findAccountHistory(@Param("accountId") Long accountId, Pageable pageable);

    default List<Transaction> findRecentTransactions(Long accountId, int count) {
        return findAccountHistory(accountId, PageRequest.of(0, count));
    }
}
