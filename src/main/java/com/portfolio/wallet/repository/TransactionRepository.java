package com.portfolio.wallet.repository;

import com.portfolio.wallet.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Every transaction where this wallet sent OR received money, newest first.
    List<Transaction> findBySenderWalletIdOrReceiverWalletIdOrderByCreatedAtDesc(
            Long senderWalletId, Long receiverWalletId);
}
