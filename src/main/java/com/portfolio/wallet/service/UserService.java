package com.portfolio.wallet.service;

import com.portfolio.wallet.dto.CreateUserRequest;
import com.portfolio.wallet.dto.DepositRequest;
import com.portfolio.wallet.dto.UserResponse;
import com.portfolio.wallet.dto.WalletResponse;
import com.portfolio.wallet.entity.User;
import com.portfolio.wallet.entity.Wallet;
import com.portfolio.wallet.exception.EmailAlreadyExistsException;
import com.portfolio.wallet.repository.UserRepository;
import com.portfolio.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;

    /** Creates a user and automatically creates their wallet, all in one transaction. */
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        // 1. Friendly error before hitting the database unique constraint.
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        // 2. Save the user.
        User user = userRepository.save(User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .build());

        // 3. Create their wallet with a zero balance.
        Wallet wallet = walletRepository.save(Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency(request.currency())
                .build());

        // 4. If a starting balance was given, reuse deposit() so it also appears in the
        //    history. It joins this same transaction, so a failure rolls back the user too.
        WalletResponse walletResponse;
        BigDecimal initial = request.initialBalance();
        if (initial != null && initial.signum() > 0) {
            walletResponse = walletService.deposit(new DepositRequest(wallet.getId(), initial));
        } else {
            walletResponse = WalletResponse.from(wallet);
        }

        return UserResponse.from(user, walletResponse);
    }
}
