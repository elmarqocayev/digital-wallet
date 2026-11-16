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

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = userRepository.save(User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .build());

        Wallet wallet = walletRepository.save(Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency(request.currency())
                .build());

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
