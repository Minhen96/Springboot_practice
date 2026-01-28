package com.example.mhpractice.features.wallet.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.features.wallet.repository.TransactionRepository;
import com.example.mhpractice.features.wallet.service.TransactionService;
import com.example.mhpractice.features.wallet.service.result.TransactionDetailsResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

        private final TransactionRepository transactionRepository;

        @Override
        public List<TransactionDetailsResult> getTransactionHistory(String walletId) {
                return transactionRepository.findAllByWalletId(UUID.fromString(walletId)).stream()
                                .map(transaction -> TransactionDetailsResult.builder()
                                                .transactionId(transaction.getTransactionId())
                                                .fromWalletId(
                                                                transaction.getFromWallet() != null
                                                                                ? transaction.getFromWallet().getId()
                                                                                                .toString()
                                                                                : "SYSTEM")
                                                .toWalletId(transaction.getToWallet() != null
                                                                ? transaction.getToWallet().getId().toString()
                                                                : "SYSTEM")
                                                .amount(transaction.getAmount())
                                                .status(transaction.getStatus().name())
                                                .creditStatus(transaction.getCreditStatus().name())
                                                .createdAt(transaction.getCreatedAt())
                                                .updatedAt(transaction.getUpdatedAt())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public TransactionDetailsResult getTransactionDetails(String transactionId) {
                return transactionRepository.findByTransactionId(transactionId)
                                .map(transaction -> TransactionDetailsResult.builder()
                                                .transactionId(transaction.getTransactionId())
                                                .fromWalletId(
                                                                transaction.getFromWallet() != null
                                                                                ? transaction.getFromWallet().getId()
                                                                                                .toString()
                                                                                : "SYSTEM")
                                                .toWalletId(transaction.getToWallet() != null
                                                                ? transaction.getToWallet().getId().toString()
                                                                : "SYSTEM")
                                                .amount(transaction.getAmount())
                                                .status(transaction.getStatus().name())
                                                .creditStatus(transaction.getCreditStatus().name())
                                                .createdAt(transaction.getCreatedAt())
                                                .updatedAt(transaction.getUpdatedAt())
                                                .build())
                                .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));
        }
}
