package com.BankingMgt.repository;

import com.BankingMgt.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // all the show in Desending order
    List<Transaction> findByAccountNumberOrderByTimestampDesc(String accountNumber);
}