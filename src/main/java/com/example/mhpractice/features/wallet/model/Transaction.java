package com.example.mhpractice.features.wallet.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.mhpractice.features.user.models.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "from_wallet_id")
    @com.fasterxml.jackson.annotation.JsonIgnore // Prevent infinite loop
    private Wallet fromWallet;

    @Column(name = "from_user_mail")
    private String fromUserMail;

    @ManyToOne
    @JoinColumn(name = "to_wallet_id")
    @com.fasterxml.jackson.annotation.JsonIgnore // Prevent infinite loop
    private Wallet toWallet;

    @Column(name = "to_user_mail")
    private String toUserMail;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransferStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "credit_status", nullable = false)
    private CreditStatus creditStatus;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TransferStatus {
        PENDING,
        FROZEN,
        SUCCESS,
        FAILED,
        CANCELLED
    }

    public enum CreditStatus {
        PENDING,
        SUCCESS,
        FAILED
    }

    public enum CancelReason {
        INSUFFICIENT_BALANCE,
        INVALID_TRANSACTION,
        INTERNAL_ERROR
    }

    public void cancel(String reason) {
        this.status = TransferStatus.CANCELLED;
        this.cancelReason = reason;
    }
}
