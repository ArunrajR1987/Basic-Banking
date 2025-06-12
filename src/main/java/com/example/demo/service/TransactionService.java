package com.example.demo.service;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.example.demo.accounts.Account;
import com.example.demo.accounts.AccountRepository;
import com.example.demo.accounts_fees.BasicAccountFee;
import com.example.demo.accounts_fees.FeeStrategy;
import com.example.demo.accounts_fees.PremiumAccountFee;
import com.example.demo.accounts_fees.StudentAccountFee;
import com.example.demo.context.TransactionValidationContext;
import com.example.demo.controller.TransactionDTO;
import com.example.demo.entity.Customer;
import com.example.demo.entity.Transaction;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.validation.BalanceValidation;
import com.example.demo.validation.KYCValidation;
import com.example.demo.validation.ValidationStrategy;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TransactionService {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionSubject transactionSubject;

    private static final String UPDATE_BALANCE_SQL =
        "UPDATE accounts SET balance = balance + ? WHERE id = ?";
    
    private static final String INSERT_TRANSACTION_SQL =
        "INSERT INTO transactions (amount, sender_account_id, receiver_account_id, status) " +
        "VALUES (?, ?, ?, ?)";

    @Autowired
    public TransactionService(JdbcTemplate jdbcTemplate, TransactionSubject transactionSubject) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionSubject = transactionSubject;
        this.validationContext = new TransactionValidationContext();
    }
    
    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    TransactionRepository transactionRepository;
    
    @Autowired
    AccountRepository accountRepository;

    List<ValidationStrategy> validationStrategies;

    private final TransactionValidationContext validationContext;

    private final Map<String, FeeStrategy> feeStrategies = Map.of(
        "BASIC", new BasicAccountFee(),
        "PREMIUM", new PremiumAccountFee(),
        "STUDENT", new StudentAccountFee()
    );

    @Autowired
    public TransactionService() {
        this.jdbcTemplate = new JdbcTemplate();
        this.transactionSubject = null;
        this.validationContext = new TransactionValidationContext();
        this.validationContext.addStrategy(new BalanceValidation());
        this.validationContext.addStrategy(new KYCValidation());
    }

    public double calculateFee(double amount, String accountType) {
        
        FeeStrategy strategy = feeStrategies.get(accountType.toUpperCase());
        if(strategy ==null) {
            throw new IllegalArgumentException("Unknown account type");
        }
        return strategy.calculateFee(amount);
    }

    @Transactional
    public String transfer(TransactionDTO dto) {
        
       

        jdbcTemplate.update(UPDATE_BALANCE_SQL,
            dto.getAmount() ,
            dto.getSenderAccountId());

        // 2. Update receiver balance
        jdbcTemplate.update(UPDATE_BALANCE_SQL,
            dto.getAmount(),
            dto.getReceiverAccountId());

        // 3. Record transaction
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                INSERT_TRANSACTION_SQL, 
                Statement.RETURN_GENERATED_KEYS);
            ps.setBigDecimal(1, (BigDecimal) dto.getAmount());
            ps.setLong(2, dto.getSenderAccountId());
            ps.setLong(3, dto.getReceiverAccountId());
            ps.setString(4, "COMPLETED");
            return ps;
        }, keyHolder);

        // 4. Notify observers
        Transaction transaction = new Transaction();
        transaction.setId(keyHolder.getKey().longValue());
        transaction.setAmount((BigDecimal) dto.getAmount());
        transaction.setStatus(TransactionStatus.COMMITTED);
        
        // Fetch the actual Account objects
        Account senderAccount = accountRepository.findById(dto.getSenderAccountId())
            .orElseThrow(() -> new RuntimeException("Sender account not found"));
        Account receiverAccount = accountRepository.findById(dto.getReceiverAccountId())
            .orElseThrow(() -> new RuntimeException("Receiver account not found"));
            
        transaction.setSender(senderAccount);
        transaction.setReceiver(receiverAccount);
        
        transactionSubject.notifyObservers(transaction);

        
        return "SUCCESS";
    }}