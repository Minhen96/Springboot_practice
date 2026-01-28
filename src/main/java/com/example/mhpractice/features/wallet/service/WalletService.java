package com.example.mhpractice.features.wallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.wallet.model.Wallet;

public interface WalletService {

    public void createWallet(User user);

    public Wallet getWalletByUserId(UUID userId);

    public void topUp(UUID walletId, BigDecimal amount, String transactionId);

    public void transferOut(UUID walletId, BigDecimal amount, String transactionId);

    public void transferIn(UUID walletId, BigDecimal amount, String transactionId);

    public void confirmTransfer(String transactionId);

    public void cancelTransfer(String transactionId, String reason);

}
