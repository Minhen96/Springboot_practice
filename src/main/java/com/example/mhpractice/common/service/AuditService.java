package com.example.mhpractice.common.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.mhpractice.common.model.AuditLog;
import com.example.mhpractice.common.repository.AuditLogRepository;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.wallet.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Audit Service for cross-feature compliance logging.
 * 
 * Creates permanent, tamper-proof records of all important events.
 * Used for:
 * - Regulatory compliance (financial regulations)
 * - Fraud investigation
 * - Dispute resolution
 * - Security analysis
 * - Business analytics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void logTopUp(Transaction txn) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action("TOP_UP")
                    .transactionId(txn.getTransactionId())
                    .toWallet(txn.getToWallet())
                    .amount(txn.getAmount())
                    .status(txn.getStatus().toString())
                    .severity("LOW")
                    .description(String.format("User top up of %s", txn.getAmount()))
                    .metadata(toJson(txn))
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit: TOP_UP - txn={}", txn.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to create audit log for top up: {}", txn.getTransactionId(), e);
        }
    }

    /**
     * Log transfer initiation
     */
    public void logTransferInitiated(Transaction txn, User fromUser, User toUser, String ipAddress) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action("TRANSFER_INITIATED")
                    .transactionId(txn.getTransactionId())
                    .fromUser(fromUser)
                    .toUser(toUser)
                    .fromWallet(txn.getFromWallet())
                    .toWallet(txn.getToWallet())
                    .amount(txn.getAmount())
                    .status(txn.getStatus().toString())
                    .severity("MEDIUM")
                    .ipAddress(ipAddress)
                    .description(String.format("User initiated transfer of %s", txn.getAmount()))
                    .metadata(toJson(txn))
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit: TRANSFER_INITIATED - txn={}", txn.getTransactionId());

        } catch (Exception e) {
            // CRITICAL: Audit logging failure should NOT break business logic
            log.error("Failed to create audit log for transfer initiation: {}", txn.getTransactionId(), e);
        }
    }

    /**
     * Log balance freeze
     */
    public void logBalanceFrozen(Transaction txn) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action("BALANCE_FROZEN")
                    .transactionId(txn.getTransactionId())
                    .fromWallet(txn.getFromWallet())
                    .amount(txn.getAmount())
                    .status(txn.getStatus().toString())
                    .severity("LOW")
                    .description(String.format("Froze %s in sender wallet", txn.getAmount()))
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit: BALANCE_FROZEN - txn={}", txn.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to create audit log for balance freeze: {}", txn.getTransactionId(), e);
        }
    }

    /**
     * Log balance credit
     */
    public void logBalanceCredited(Transaction txn) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action("BALANCE_CREDITED")
                    .transactionId(txn.getTransactionId())
                    .toWallet(txn.getToWallet())
                    .amount(txn.getAmount())
                    .status(txn.getCreditStatus().toString())
                    .severity("LOW")
                    .description(String.format("Credited %s to receiver wallet (unreleased)", txn.getAmount()))
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit: BALANCE_CREDITED - txn={}", txn.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to create audit log for balance credit: {}", txn.getTransactionId(), e);
        }
    }

    /**
     * Log transfer success
     */
    public void logTransferSuccess(Transaction txn) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action("TRANSFER_SUCCESS")
                    .transactionId(txn.getTransactionId())
                    .fromWallet(txn.getFromWallet())
                    .toWallet(txn.getToWallet())
                    .amount(txn.getAmount())
                    .status("SUCCESS")
                    .severity("LOW")
                    .description(String.format("Transfer completed successfully: %s", txn.getAmount()))
                    .metadata(toJson(txn))
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit: TRANSFER_SUCCESS - txn={}", txn.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to create audit log for transfer success: {}", txn.getTransactionId(), e);
        }
    }

    /**
     * Log transfer rollback (HIGH severity - needs investigation!)
     */
    public void logRollback(Transaction txn) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action("TRANSFER_ROLLBACK")
                    .transactionId(txn.getTransactionId())
                    .fromWallet(txn.getFromWallet())
                    .toWallet(txn.getToWallet())
                    .amount(txn.getAmount())
                    .status("CANCELLED")
                    .severity("HIGH") // Rollbacks are important!
                    .description(String.format("Transfer rolled back: %s - Reason: %s",
                            txn.getAmount(), txn.getCancelReason()))
                    .metadata(toJson(txn))
                    .build();

            auditLogRepository.save(auditLog);
            log.warn("Audit: TRANSFER_ROLLBACK - txn={}, reason={}",
                    txn.getTransactionId(), txn.getCancelReason());

        } catch (Exception e) {
            log.error("Failed to create audit log for rollback: {}", txn.getTransactionId(), e);
        }
    }

    /**
     * Log transfer failure
     */
    public void logTransferFailed(String transactionId, String reason) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action("TRANSFER_FAILED")
                    .transactionId(transactionId)
                    .status("FAILED")
                    .severity("MEDIUM")
                    .description("Transfer failed: " + reason)
                    .build();

            auditLogRepository.save(auditLog);
            log.warn("Audit: TRANSFER_FAILED - txn={}, reason={}", transactionId, reason);

        } catch (Exception e) {
            log.error("Failed to create audit log for transfer failure: {}", transactionId, e);
        }
    }

    /**
     * Helper: Convert object to JSON for metadata storage
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
