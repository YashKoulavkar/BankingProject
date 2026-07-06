package com.BankingMgt.controller;

import com.BankingMgt.config.JwtUtils;
import com.BankingMgt.entity.Account;
import com.BankingMgt.entity.Transaction;
import com.BankingMgt.repository.TransactionRepository;
import com.BankingMgt.service.AccountService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
public class AccountController {

    private  final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private AuthenticationManager authenticationManager;
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            // 1. Authenticate user credentials safely
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

            // 2. If successful, generate the token
            String token = jwtUtils.generateToken(username);

            Map<String, String> response = new HashMap<>();
            response.put("token", token);

            return ResponseEntity.ok(response);

        } catch (org.springframework.security.core.AuthenticationException e) {
            // If username or password is wrong, return a clear error message
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid username or password!");
            errorResponse.put("details", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        return new ResponseEntity<>(accountService.createAccount(account), HttpStatus.CREATED);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }

    @PutMapping("/{accountNumber}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable String accountNumber, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        return ResponseEntity.ok(accountService.deposit(accountNumber, amount));
    }

    @PutMapping("/{accountNumber}/withdraw")
    public ResponseEntity<Account> withdraw(@PathVariable String accountNumber, @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        return ResponseEntity.ok(accountService.withdraw(accountNumber, amount));
    }

    @PutMapping("/transfer")
    public ResponseEntity<String> transferFunds(@RequestBody Map<String, Object> request) {
        String sourceAccount = (String) request.get("sourceAccountNumber");
        String targetAccount = (String) request.get("targetAccountNumber");

        // String कडून BigDecimal मध्ये सुरक्षित कन्व्हर्ट करणे
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        accountService.transferFunds(sourceAccount, targetAccount, amount);
        return ResponseEntity.ok("Transaction Successful! Funds transferred successfully.");
    }


    // delete account

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Map<String, String>> deleteAccount(@PathVariable String accountNumber) {
        accountService.deleteAccount(accountNumber);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Account number " + accountNumber + " has been closed/deleted successfully.");

        return ResponseEntity.ok(response);
    }


    //Getting account Statement
    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<?> getStatement(@PathVariable String accountNumber) {
        // Extract the logged-in username from the JWT context
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loggedInUsername = (principal instanceof UserDetails) ?
                ((UserDetails) principal).getUsername() : principal.toString();

        // Fetch the account to check ownership
        Account account = accountService.getAccount(accountNumber);

        // 🔒 If a regular user tries to look at someone else's statement, return 403 Forbidden
        if (!account.getAccountHolderName().equalsIgnoreCase(loggedInUsername) && !loggedInUsername.equals("admin")) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Access Denied");
            errorResponse.put("message", "You can only view your own transaction statements!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        List<Transaction> statement = transactionRepository.findByAccountNumberOrderByTimestampDesc(accountNumber);
        return ResponseEntity.ok(statement);
    }
}
