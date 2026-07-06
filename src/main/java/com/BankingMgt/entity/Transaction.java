package com.BankingMgt.entity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String accountNumber;
    private String transactionType; // DEPOSIT, WITHDRAW, TRANSFER
    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String description;

}