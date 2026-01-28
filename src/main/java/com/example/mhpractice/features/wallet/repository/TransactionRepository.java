package com.example.mhpractice.features.wallet.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.mhpractice.features.wallet.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
            "FROM Transaction t WHERE t.transactionId = :transactionId " +
            "AND t.creditStatus = 'SUCCESS'")
    boolean isCreditCompleted(@Param("transactionId") String transactionId);

    @Query("SELECT t FROM Transaction t WHERE t.fromWallet.id = :walletId OR t.toWallet.id = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByWalletId(@Param("walletId") UUID walletId);

}
