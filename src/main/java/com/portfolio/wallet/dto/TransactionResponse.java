package com.portfolio.wallet.dto;

import com.portfolio.wallet.entity.Transaction;
import com.portfolio.wallet.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        Long senderWalletId,
        Long receiverWalletId,
        BigDecimal amount,
        TransactionStatus status,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction tx) {
        return new TransactionResponse(
                tx.getId(), tx.getSenderWalletId(), tx.getReceiverWalletId(),
                tx.getAmount(), tx.getStatus(), tx.getCreatedAt());
    }
}
