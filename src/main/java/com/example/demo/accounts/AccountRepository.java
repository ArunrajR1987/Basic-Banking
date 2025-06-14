package com.example.demo.accounts;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long>{


    @Query("SELECT a FROM Account a WHERE a.customer.id = :customerId")
    List<Account> findByCustomerId(@Param("customerId") Long customerId);

    /**
     * The @Lock(LockModeType.PESSIMISTIC_WRITE) annotation is used to implement database-level locking
     * for concurrent transaction management. It's typically used in financial applications like this
     * banking system to prevent race conditions during critical operations.
     * 
     * Why it's used:
     * 1. Prevents concurrent modifications: When multiple transactions try to update the same account
     *    simultaneously (e.g., two withdrawals at the same time), this lock ensures only one transaction
     *    can modify the account at a time.
     * 2. Ensures data integrity: For banking operations, it's critical that balance calculations are
     *    accurate and not subject to race conditions that could lead to incorrect balances.
     * 3. Prevents "lost updates": Without this lock, one transaction might overwrite changes made by another.
     * 
     * PESSIMISTIC_WRITE vs Other Lock Types:
     * - PESSIMISTIC_WRITE: Acquires an exclusive lock, preventing other transactions from both reading and
     *   writing to the locked data. This is the strongest lock type and is used here because we need absolute
     *   certainty that no other transaction can interfere with account balance operations.
     * - PESSIMISTIC_READ: Would only prevent other write operations but allow concurrent reads, which isn't
     *   sufficient for financial transactions where even reading an intermediate state could lead to incorrect
     *   decisions.
     * - OPTIMISTIC: Would use version checking instead of locks, detecting conflicts only at commit time.
     *   This is too risky for financial transactions as it would allow operations to proceed based on
     *   potentially stale data and only fail later.
     * - NONE: Would provide no concurrency control, which is unacceptable for financial data.
     * 
     * For banking applications, PESSIMISTIC_WRITE is preferred because:
     * 1. Financial accuracy is more important than performance
     * 2. The cost of a conflict (incorrect balance) is extremely high
     * 3. The "SELECT FOR UPDATE" SQL pattern (implemented by PESSIMISTIC_WRITE) is the industry standard
     *    for financial transaction processing
     * 
     * In this specific case, it's used to lock an account record before performing financial transactions,
     * ensuring that the account balance remains consistent even under high concurrency scenarios.
     * The method name "findByIdForUpdate" clearly indicates its purpose - finding an account with the
     * intention to update it safely.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :accountId")
    Optional<Account> findByIdForUpdate(@Param("accountId") Long accountId);
}
