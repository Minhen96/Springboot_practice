package com.example.mhpractice.features.wallet.service;

import com.example.mhpractice.features.wallet.service.result.TransactionDetailsResult;
import java.util.List;

public interface TransactionService {
    public List<TransactionDetailsResult> getTransactionHistory(String walletId);

    public TransactionDetailsResult getTransactionDetails(String transactionId);
}
