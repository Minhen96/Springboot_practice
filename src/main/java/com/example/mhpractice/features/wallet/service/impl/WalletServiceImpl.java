package com.example.mhpractice.features.wallet.service.impl;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.common.exception.ErrorCode;
import com.example.mhpractice.common.service.AuditService;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.repository.UserRepository;
import com.example.mhpractice.features.wallet.model.Transaction;
import com.example.mhpractice.features.wallet.model.Wallet;
import com.example.mhpractice.features.wallet.model.Transaction.CreditStatus;
import com.example.mhpractice.features.wallet.model.Transaction.TransferStatus;
import com.example.mhpractice.features.wallet.repository.TransactionRepository;
import com.example.mhpractice.features.wallet.repository.WalletRepository;
import com.example.mhpractice.features.wallet.service.WalletService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final RedissonClient redissonClient;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    @Override
    public void createWallet(User user) {
        walletRepository.findByUserId(user.getId()).ifPresentOrElse((wallet -> {
            throw new BusinessException(ErrorCode.WALLET_ALREADY_EXISTS);
        }), () -> {
            Wallet wallet = Wallet.builder()
                    .user(user)
                    .build();
            walletRepository.save(wallet);
        });
    }

    @Override
    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
    }

    @Override
    @Transactional
    public void topUp(UUID walletId, BigDecimal amount, String transactionId) {
        RLock lock = redissonClient.getLock("wallet:" + walletId);
        try {
            lock.lock(10, TimeUnit.SECONDS);

            if (transactionRepository.existsByTransactionId(transactionId)) {
                return;
            }

            Wallet wallet = walletRepository.findByIdWithLock(walletId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

            wallet.addBalance(amount);
            walletRepository.save(wallet);

            Transaction record = Transaction.builder()
                    .transactionId(transactionId)
                    .fromWallet(null)
                    .toWallet(wallet)
                    .amount(amount)
                    .status(TransferStatus.SUCCESS)
                    .creditStatus(CreditStatus.SUCCESS)
                    .build();

            transactionRepository.save(record);

            auditService.logTopUp(record);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ========================================
    // STEP 1: Debit/Withdraw (transferFrom)
    // ========================================
    @Override
    @Transactional
    public void transferOut(UUID walletId, BigDecimal amount, String transactionId) {

        // 1. Distributed lock on sender wallet
        RLock lock = redissonClient.getLock("wallet:" + walletId);

        try {
            lock.lock(10, TimeUnit.SECONDS);

            // 2. Check idempotency (prevent duplicate debit)
            if (transactionRepository.existsByTransactionId(transactionId) && transactionRepository
                    .findByTransactionId(transactionId).get().getStatus() != TransferStatus.PENDING) {
                // return
                // transactionRepository.findByTransactionId(transactionId).orElseThrow();
                return;
            }

            // 3. Pessimistic lock + load wallet
            Wallet wallet = walletRepository.findByIdWithLock(walletId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

            // 4. Validation
            if (!wallet.hasSufficientBalance(amount)) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
            }

            // 5. Freeze the amount (NOT deduct yet!)
            wallet.setFrozenBalance(wallet.getFrozenBalance().add(amount));
            walletRepository.save(wallet);

            // 6. Create or Update transaction record
            Transaction record;
            var existingTxn = transactionRepository.findByTransactionId(transactionId);

            if (existingTxn.isPresent()) {
                record = existingTxn.get();
                // Check consistency
                if (record.getStatus() != TransferStatus.PENDING) {
                    // Should have been caught by idempotency check, but safe guard
                    throw new BusinessException(ErrorCode.TRANSACTION_INTERNAL_ERROR);
                }
                record.setStatus(TransferStatus.FROZEN);
                record.setCreditStatus(CreditStatus.PENDING);
                record.setFromUserMail(wallet.getUser().getEmail()); // Ensure mail is set
            } else {
                // Fallback: If for some reason init didn't happen (legacy flow?), create it
                record = Transaction.builder()
                        .transactionId(transactionId)
                        .fromWallet(wallet)
                        .fromUserMail(wallet.getUser().getEmail())
                        .amount(amount)
                        .creditStatus(CreditStatus.PENDING)
                        .status(TransferStatus.FROZEN) // Not SUCCESS yet!
                        .build();
            }

            transactionRepository.save(record);

            auditService.logBalanceFrozen(record);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ========================================
    // STEP 2: Credit/Deposit (transferTo)
    // ========================================
    @Override
    @Transactional
    public void transferIn(UUID walletId, BigDecimal amount, String transactionId) {

        // 1. Lock receiver wallet
        RLock lock = redissonClient.getLock("wallet:" + walletId);

        try {
            lock.lock(10, TimeUnit.SECONDS);

            Transaction record = transactionRepository.findByTransactionId(transactionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.TRANSACTION_NOT_FOUND));

            // 2. Check idempotency
            if (transactionRepository.isCreditCompleted(transactionId)) {
                return; // Already credited
            }

            // 3. Load wallet with lock
            Wallet wallet = walletRepository.findByIdWithLock(walletId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

            // 4. Credit the amount
            wallet.addUnreleasedBalance(amount);
            walletRepository.save(wallet);

            // 5. Update transaction status
            record.setToWallet(wallet);
            record.setToUserMail(wallet.getUser().getEmail());
            record.setCreditStatus(CreditStatus.SUCCESS);
            transactionRepository.save(record);

            auditService.logBalanceCredited(record);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ========================================
    // STEP 3: Confirm Transfer (Saga Completion)
    // ========================================
    @Override
    @Transactional
    public void confirmTransfer(String transactionId) {

        RLock lock = redissonClient.getLock("transaction:" + transactionId);

        try {
            lock.lock(10, TimeUnit.SECONDS);

            Transaction record = transactionRepository.findByTransactionId(transactionId)
                    .orElseThrow();

            // Check both steps completed
            if (record.getStatus() != TransferStatus.FROZEN
                    || record.getCreditStatus() != CreditStatus.SUCCESS) {
                throw new IllegalStateException("Transfer not ready to confirm");
            }

            // 1. Deduct sender's frozen balance and real balance
            Wallet fromWallet = walletRepository.findByIdWithLock(record.getFromWallet().getId())
                    .orElseThrow();
            fromWallet.clearFrozenBalance(record.getAmount());

            walletRepository.save(fromWallet);

            // 2. Release from unreleased balance
            Wallet toWallet = walletRepository.findByIdWithLock(record.getToWallet().getId())
                    .orElseThrow();
            toWallet.releaseUnreleasedBalance(record.getAmount());
            walletRepository.save(toWallet);

            // 3. Mark transaction complete
            record.setStatus(TransferStatus.SUCCESS);
            transactionRepository.save(record);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ========================================
    // STEP 4: Compensating Transaction (Rollback)
    // ========================================
    @Override
    @Transactional
    public void cancelTransfer(String transactionId, String reason) {

        RLock lock = redissonClient.getLock("transaction:" + transactionId);

        try {
            lock.lock(10, TimeUnit.SECONDS);

            var transactionOpt = transactionRepository.findByTransactionId(transactionId);
            if (transactionOpt.isEmpty()) {
                // If transaction record doesn't exist, it means failure happened before
                // persistence.
                // Nothing to rollback.
                return;
            }
            Transaction record = transactionOpt.get();

            // Unfreeze sender's balance
            Wallet fromWallet = walletRepository.findByIdWithLock(record.getFromWallet().getId())
                    .orElseThrow();
            fromWallet.setFrozenBalance(
                    fromWallet.getFrozenBalance().subtract(record.getAmount()));
            walletRepository.save(fromWallet);

            // Release from unreleased balance
            Wallet toWallet = walletRepository.findByIdWithLock(record.getToWallet().getId())
                    .orElseThrow();
            toWallet.setUnreleasedBalance(toWallet.getUnreleasedBalance().subtract(record.getAmount()));
            walletRepository.save(toWallet);

            // If credit already happened, reverse it
            if (record.getCreditStatus() == CreditStatus.SUCCESS) {
                toWallet.setBalance(toWallet.getBalance().subtract(record.getAmount()));
                walletRepository.save(toWallet);
            }

            // Mark as cancelled
            record.setStatus(TransferStatus.CANCELLED);
            record.setCancelReason(reason);
            transactionRepository.save(record);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
