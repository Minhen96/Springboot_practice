package com.example.mhpractice.features.wallet.service.result;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetailsResult {
    private String transactionId;
    private String fromWalletId;
    private String toWalletId;
    private BigDecimal amount;
    private String status;
    private String creditStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
