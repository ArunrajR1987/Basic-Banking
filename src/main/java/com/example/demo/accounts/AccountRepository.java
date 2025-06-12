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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :accountId")
    Optional<Account> findByIdForUpdate(@Param("accountId") Long accountId);
}
