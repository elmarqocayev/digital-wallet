package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.TransactionResponse;
import com.portfolio.wallet.dto.TransferRequest;
import com.portfolio.wallet.entity.Transaction;
import com.portfolio.wallet.entity.TransactionStatus;
import com.portfolio.wallet.entity.Wallet;
import com.portfolio.wallet.exception.CurrencyMismatchException;
import com.portfolio.wallet.exception.InsufficientBalanceException;
import com.portfolio.wallet.exception.InvalidTransferException;
import com.portfolio.wallet.exception.WalletNotFoundException;
import com.portfolio.wallet.repository.TransactionRepository;
import com.portfolio.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionRecorder transactionRecorder;

    /**
     * Moves money from one wallet to another.
     *
     * @Transactional means: if ANY runtime exception is thrown inside this method,
     * every database change made so far is rolled back. So we can never end up with
     * money subtracted from the sender but not added to the receiver.
     */
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        Long senderId = request.senderWalletId();
        Long receiverId = request.receiverWalletId();
        BigDecimal amount = request.amount();

        // STEP 1: A wallet cannot send money to itself.
        if (senderId.equals(receiverId)) {
            throw new InvalidTransferException("Sender and receiver wallets must be different");
        }

        // STEP 2: Lock both wallets. We always lock the SMALLER id first.
        // Why? If A->B and B->A arrive at the same moment and each locked its own
        // wallet first, they would wait for each other forever (deadlock).
        // A fixed locking order makes that impossible.
        Long firstId = Math.min(senderId, receiverId);
        Long secondId = Math.max(senderId, receiverId);
        Wallet first = lockWallet(firstId);
        Wallet second = lockWallet(secondId);

        // STEP 3: Work out which locked wallet is the sender and which is the receiver.
        Wallet sender = senderId.equals(first.getId()) ? first : second;
        Wallet receiver = (sender == first) ? second : first;

        // STEP 4: Currencies must match (we do not do exchange rates in this project).
        if (sender.getCurrency() != receiver.getCurrency()) {
            transactionRecorder.recordFailure(senderId, receiverId, amount);
            throw new CurrencyMismatchException(sender.getCurrency(), receiver.getCurrency());
        }

        // STEP 5: The sender must have enough money.
        // compareTo (not equals!) is the correct way to compare BigDecimals.
        if (sender.getBalance().compareTo(amount) < 0) {
            transactionRecorder.recordFailure(senderId, receiverId, amount);
            throw new InsufficientBalanceException(senderId, sender.getBalance(), amount);
        }

        // STEP 6: Move the money. Both changes are written together when the
        // transaction commits (or neither is, if something fails).
        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        // STEP 7: Record the successful transfer in the history.
        Transaction saved = transactionRepository.save(Transaction.builder()
                .senderWalletId(senderId)
                .receiverWalletId(receiverId)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .build());

        return TransactionResponse.from(saved);
    }

    private Wallet lockWallet(Long walletId) {
        return walletRepository.findByIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }
}
