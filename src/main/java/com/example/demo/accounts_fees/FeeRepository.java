package com.example.demo.accounts_fees;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FeeRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public double getFeeRate(String accountType, String feeType) {
        String sql = "SELECT rate FROM fee_structures WHERE account_type = ? AND fee_type = ?";
        Double result = jdbcTemplate.queryForObject(sql, Double.class, accountType, feeType);
        return result != null ? result : 0.0;
    }}
