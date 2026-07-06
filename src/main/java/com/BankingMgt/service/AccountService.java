package com.BankingMgt.service;

import com.BankingMgt.entity.Account;

import java.math.BigDecimal;

public interface AccountService {
    Account createAccount(Account account);
    Account getAccount(String accountNumber);
    Account deposit(String accountNumber, BigDecimal amount);
    Account withdraw(String accountNumber, BigDecimal amount);
    void transferFunds(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount);
    void deleteAccount(String accountNumber);
}