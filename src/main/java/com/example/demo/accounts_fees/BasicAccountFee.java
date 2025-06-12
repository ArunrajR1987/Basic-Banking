package com.example.demo.accounts_fees;


import org.springframework.beans.factory.annotation.Autowired;

public class BasicAccountFee implements FeeStrategy {

    private final FeeRepository feeRepository;

    public BasicAccountFee() {
        this.feeRepository = null;
    }

    @Autowired
    public BasicAccountFee(FeeRepository feeRepository) {
        this.feeRepository = feeRepository;
    }

    @Override
    public double calculateFee(double amount) {
        double rate = feeRepository.getFeeRate("BASIC", "TRANSACTION");
        return amount * rate;
    }
    @Override
    public String getFeeType() {
        return "BASIC_TRANSACTION_FEE";
    }
    
}
