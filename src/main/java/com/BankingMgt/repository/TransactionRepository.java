package com.BankingMgt.repository;

import com.BankingMgt.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // विशिष्ट अकाउंट नंबरचे सर्व व्यवहार नवीन ते जुने (Desc) या क्रमाने आणण्यासाठी
    List<Transaction> findByAccountNumberOrderByTimestampDesc(String accountNumber);
}