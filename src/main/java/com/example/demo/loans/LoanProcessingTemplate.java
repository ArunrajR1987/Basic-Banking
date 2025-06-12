package com.example.demo.loans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class LoanProcessingTemplate {
    protected final JdbcTemplate jdbcTemplate;
    
    private static final String UPDATE_LOAN_STATUS_SQL =
        "UPDATE loans SET status = ? WHERE id = ?";

    @Autowired
    protected LoanProcessingTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public final void processLoan(LoanApplication loan) {
        verifyIdentity(loan);
        checkCreditHistory(loan);
        validateIncome(loan);
        approveLoan(loan);
        updateLoanStatus(loan);
    }
    protected abstract void verifyIdentity(LoanApplication loan);
    protected abstract void checkCreditHistory(LoanApplication loan);
    protected abstract void validateIncome(LoanApplication loan);

    protected void approveLoan(LoanApplication loan) {
        loan.setStatus(LoanStatus.APPROVED);
    }

    protected void updateLoanStatus(LoanApplication loan) {
        jdbcTemplate.update(UPDATE_LOAN_STATUS_SQL,
            loan.getStatus().toString(),
            loan.getId());
    }
}