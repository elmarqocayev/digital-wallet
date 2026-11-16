package com.portfolio.wallet.service;

import com.portfolio.wallet.entity.Transaction;
import com.portfolio.wallet.entity.TransactionStatus;
import com.portfolio.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Why a separate class? When a transfer fails we throw an exception, which rolls back
 * the transfer's transaction, and that would also erase a FAILED history row saved
 * inside it. REQUIRES_NEW runs in its own independent transaction that commits
 * immediately, so the FAILED record stays. It must be a separate bean: Spring applies
 * @Transactional through a proxy, and a method calling another method of the same
 * class bypasses that proxy.
 */
@Service
@RequiredArgsConstructor
public class TransactionRecorder {

    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long senderWalletId, Long receiverWalletId, BigDecimal amount) {
        transactionRepository.save(Transaction.builder()
                .senderWalletId(senderWalletId)
                .receiverWalletId(receiverWalletId)
                .amount(amount)
                .status(TransactionStatus.FAILED)
                .build());
    }
}
