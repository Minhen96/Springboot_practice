package com.example.mhpractice.features.wallet.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mhpractice.common.http.annotation.StandardReponseBody;
import com.example.mhpractice.features.wallet.service.TransactionService;
import com.example.mhpractice.features.wallet.service.result.TransactionDetailsResult;

import com.example.mhpractice.features.user.service.UserService;
import com.example.mhpractice.features.wallet.service.WalletService;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.wallet.model.Wallet;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@StandardReponseBody
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final WalletService walletService;

    @GetMapping("/transactions")
    public List<TransactionDetailsResult> getTransactionHistory(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        Wallet wallet = walletService.getWalletByUserId(user.getId());
        return transactionService.getTransactionHistory(wallet.getId().toString());
    }

    @GetMapping("/transactions/{transactionId}")
    public TransactionDetailsResult getTransactionDetails(@PathVariable String transactionId) {
        return transactionService.getTransactionDetails(transactionId);
    }
}
