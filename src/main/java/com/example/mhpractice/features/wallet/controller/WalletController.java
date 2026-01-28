package com.example.mhpractice.features.wallet.controller;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mhpractice.common.http.annotation.StandardReponseBody;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.service.UserService;
import com.example.mhpractice.features.wallet.controller.request.TopupRequest;
import com.example.mhpractice.features.wallet.controller.request.TransferRequest;
import com.example.mhpractice.features.wallet.controller.response.BalanceResponse;
import com.example.mhpractice.features.wallet.event.TransferRequestEvent;
import com.example.mhpractice.features.wallet.model.Transaction;
import com.example.mhpractice.features.wallet.model.Wallet;
import com.example.mhpractice.features.wallet.model.Transaction.TransferStatus;
import com.example.mhpractice.features.wallet.repository.TransactionRepository;
import com.example.mhpractice.features.wallet.service.WalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @StandardReponseBody
    @GetMapping("/balance")
    public BalanceResponse getBalance(Authentication authentication) {
        String currentEmail = authentication.getName();
        User currentUser = userService.getUserByEmail(currentEmail);
        Wallet currentUserWallet = walletService.getWalletByUserId(currentUser.getId());
        return BalanceResponse.builder()
                .balance(currentUserWallet.getBalance())
                .walletId(currentUserWallet.getId().toString())
                .build();
    }

    @PostMapping("/topup")
    public boolean topUp(Authentication authentication, @Valid @RequestBody TopupRequest request) {
        User currentUser = userService.getUserByEmail(authentication.getName());
        Wallet currentUserWallet = walletService.getWalletByUserId(currentUser.getId());
        String transactionId = UUID.randomUUID().toString();
        walletService.topUp(currentUserWallet.getId(), request.getAmount(), transactionId);
        return true;
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequest request,
            Authentication authentication) {

        User currentUser = userService.getUserByEmail(authentication.getName());
        User targetUser = userService.getUserByEmail(request.getEmail());
        Wallet fromWallet = walletService.getWalletByUserId(currentUser.getId());
        Wallet toWallet = walletService.getWalletByUserId(targetUser.getId());

        String transactionId = UUID.randomUUID().toString();
        initiateTransfer(fromWallet, toWallet, request.getAmount(), transactionId);

        kafkaTemplate.executeInTransaction(operations -> {
            operations.send("transfer.events.request", TransferRequestEvent.of(fromWallet.getId(), toWallet.getId(),
                    request.getAmount(), transactionId));
            return null;
        });

        return ResponseEntity.ok(Map.of("message", "Transfer initiated successfully", "transactionId", transactionId));
    }

    private void initiateTransfer(Wallet fromWallet, Wallet toWallet, BigDecimal amount, String transactionId) {
        Transaction txn = Transaction.builder()
                .fromWallet(fromWallet)
                .fromUserMail(fromWallet.getUser().getEmail())
                .toWallet(toWallet)
                .toUserMail(toWallet.getUser().getEmail())
                .amount(amount)
                .transactionId(transactionId)
                .status(TransferStatus.PENDING)
                .creditStatus(com.example.mhpractice.features.wallet.model.Transaction.CreditStatus.PENDING)
                .build();
        transactionRepository.save(txn);
    }

}
