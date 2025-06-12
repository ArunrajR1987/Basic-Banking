package com.example.demo.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.demo.entity.Transaction;

public class DatabaseAuditObserver implements TransactionObserver {
    
    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_AUDIT_SQL = 
        "INSERT INTO transaction_audit (transaction_id, old_status, new_status, notes) " +
        "VALUES (?, ?, ?, ?)";

    @Autowired
    public DatabaseAuditObserver(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void update(Transaction transaction) {
        jdbcTemplate.update(INSERT_AUDIT_SQL,
            transaction.getId(),
            transaction.getOldStatus(),
            transaction.getStatus(),
            "Processed by " + getObserverType());
    }

    @Override
    public String getObserverType() {
        return "DATABASE_AUDIT";
    }
}
