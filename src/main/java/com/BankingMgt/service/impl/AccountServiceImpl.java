package com.BankingMgt.service.impl;

import com.BankingMgt.entity.Account;
import com.BankingMgt.entity.Transaction;
import com.BankingMgt.exception.AccountException;
import com.BankingMgt.repository.AccountRepository;
import com.BankingMgt.repository.TransactionRepository;
import com.BankingMgt.service.AccountService;
import com.BankingMgt.service.SmsService;
import jakarta.persistence.Id;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final SmsService smsService;


    // create acc
    @Override
    public Account createAccount(Account account) {
        // १. save in DB
        Account savedAccount = accountRepository.save(account);

        //  build msg
        String smsMessage = String.format(
                "Dear %s, Your Bank Account %s has been created successfully. Current Balance: INR %s.",
                savedAccount.getAccountHolderName(),
                savedAccount.getAccountNumber(),
                savedAccount.getBalance()
        );


        String userPhoneNumber = savedAccount.getPhoneNumber();

        // Must add +91
        if (!userPhoneNumber.startsWith("+")) {
            userPhoneNumber = "+91" + userPhoneNumber;
        }

        smsService.sendSms(userPhoneNumber, smsMessage);

        return savedAccount;
    }

    // deposit
    @Override
    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);

        // ३. ट्रान्झॅक्शन हिस्टरी सेव्ह करा (Builder पॅटर्न वापरून क्लीन कोड)
        Transaction txn = Transaction.builder()
                .accountNumber(accountNumber)
                .transactionType("DEPOSIT")
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .description("Cash Deposit")
                .build();
        transactionRepository.save(txn);

        // SMS पाठवणे
        String smsMessage = String.format("Account %s Credited with INR %s. Balance: INR %s.",
                accountNumber, amount, updatedAccount.getBalance());
        smsService.sendSms(updatedAccount.getPhoneNumber(), smsMessage);

        return updatedAccount;
    }

    // withdraw
    @Override
    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account account = getAccount(accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new AccountException("Transaction Failed! Insufficient balance in account: " + accountNumber);

        }

        account.setBalance(account.getBalance().subtract(amount));
        Account updatedAccount = accountRepository.save(account);

        // ४. save transaltion history
        Transaction txn = Transaction.builder()
                .accountNumber(accountNumber)
                .transactionType("WITHDRAW")
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .description("Cash Withdrawal")
                .build();
        transactionRepository.save(txn);

        // SMS send
        String smsMessage = String.format("Account %s Debited with INR %s. Balance: INR %s.",
                accountNumber, amount, updatedAccount.getBalance());
        smsService.sendSms(updatedAccount.getPhoneNumber(), smsMessage);

        return updatedAccount;
    }

    @Override
    @Transactional
    public void transferFunds(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount)
    {
        // 1. Fetch both accounts from the database
        Account sourceAccount = getAccount(sourceAccountNumber);
        Account targetAccount = getAccount(targetAccountNumber);

        // 2. Check if the source account has sufficient balance
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new AccountException("Transfer Failed! Insufficient balance in source account: " + sourceAccountNumber);
        }

        // 3. Deduct the amount from the source account
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));

        // 4. Add the amount to the target account
        targetAccount.setBalance(targetAccount.getBalance().add(amount));

        // 5. Save both updated accounts back to the database
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);
    }


    @Override
    public void deleteAccount(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Cannot delete! Account number " + accountNumber + " does not exist."));
        accountRepository.delete(account);
    }



    /*// get account
    @Override
    public Account getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account number " + accountNumber + " does not exist!"));
        // RuntimeException ऐवजी AccountException वापरा
    }
*/

    // 💡 NEW HELPER METHOD: Verifies if the authenticated token holder owns the account
    private void verifyUserAccess(String accountNumber) {
        // Extract the logged-in username from the Spring Security context
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loggedInUsername;

        if (principal instanceof UserDetails) {
            loggedInUsername = ((UserDetails) principal).getUsername();
        } else {
            loggedInUsername = principal.toString();
        }

        // Check if the requested account exists
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account number " + accountNumber + " does not exist!"));

        // 🔒 SECURITY CHECK: If the account holder's name/username doesn't match the token, block it!
        // Note: If you map username to account numbers differently, change this comparison logic accordingly.
        if (!account.getAccountHolderName().equalsIgnoreCase(loggedInUsername) && !loggedInUsername.equals("admin")) {
            throw new AccountException("Access Denied! You are not authorized to view this account's details.");
        }
    }

    @Override
    public Account getAccount(String accountNumber) {
        // 💡 Add the check here before returning the balance details
        verifyUserAccess(accountNumber);

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException("Account number " + accountNumber + " does not exist!"));
    }


}


