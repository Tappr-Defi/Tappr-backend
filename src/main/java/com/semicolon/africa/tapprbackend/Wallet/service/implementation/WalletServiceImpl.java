package com.semicolon.africa.tapprbackend.Wallet.service.implementation;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.semicolon.africa.tapprbackend.Wallet.dtos.requests.CreateWalletRequest;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.CreateWalletResponse;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.WalletBalanceResponse;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletStatus;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import com.semicolon.africa.tapprbackend.Wallet.exceptions.InsufficientBalanceException;
import com.semicolon.africa.tapprbackend.Wallet.exceptions.WalletNotFoundException;
import com.semicolon.africa.tapprbackend.Wallet.service.interfaces.WalletService;
import com.semicolon.africa.tapprbackend.Wallet.utils.WalletUtils;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.transaction.services.interfaces.SuiRateService;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.exceptions.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class WalletServiceImpl implements WalletService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final JwtUtil jwtUtil;
    private final SuiRateService suiRateService;

    public WalletServiceImpl(UserRepository userRepository,
                             WalletRepository walletRepository,
                             JwtUtil jwtUtil,
                             SuiRateService suiRateService) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.jwtUtil = jwtUtil;
        this.suiRateService = suiRateService;
    }

    @Override
    public CreateWalletResponse createWalletForUser(String jwtToken, CreateWalletRequest createWalletRequest) {
        UUID userId = UUID.fromString(jwtUtil.extractUserId(jwtToken));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        createWalletIfNotExists(user);

        WalletCurrency preferredCurrency = WalletUtils.resolveCurrencyFromPhoneNumber(user.getPhoneNumber());

        Wallet wallet = walletRepository.findByUserAndCurrencyType(user, preferredCurrency)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        return mapToCreateWalletResponse(wallet);
    }

    @Override
    public void createWalletIfNotExists(User user) {
        if (!walletRepository.existsByUserAndCurrencyType(user, WalletCurrency.NGN)) {
            log.info("Creating Fiat wallet for user: {}", user.getEmail());
            createFiatWallet(user);
        }

        if (!walletRepository.existsByUserAndCurrencyType(user, WalletCurrency.SUI)) {
            log.info("Creating Crypto wallet for user: {}", user.getEmail());
            createSuiWallet(user);
        }
    }

    @Override
    public CreateWalletResponse createWallet(User user) {
        createWalletIfNotExists(user);
        Wallet fiatWallet = walletRepository.findByUserAndCurrencyType(user, WalletCurrency.NGN)
                .orElseThrow(() -> new WalletNotFoundException("Fiat wallet not found after creation"));
        return mapToCreateWalletResponse(fiatWallet);
    }

    public Wallet createFiatWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletType(WalletType.FIAT);
        wallet.setCurrencyType(WalletCurrency.NGN);
        wallet.setAccountNumber(WalletUtils.generateAccountNumberFromPhoneNumber(user.getPhoneNumber()));
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        return walletRepository.save(wallet);
    }

    public Wallet createSuiWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletType(WalletType.CRYPTO);
        wallet.setCurrencyType(WalletCurrency.SUI);
        wallet.setWalletAddress("sui_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        wallet.setTokenSymbol("SUI");
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        return walletRepository.save(wallet);
    }

    @Override
    public WalletBalanceResponse getUserWalletBalances(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Wallet fiat = getWalletByType(user, WalletCurrency.NGN);
        Wallet sui = getWalletByType(user, WalletCurrency.SUI);

        BigDecimal exchangeRate = suiRateService.getSuiToNgnRate();

        return WalletBalanceResponse.builder()
                .fiatBalance(fiat.getBalance())
                .fiatCurrency("NGN")
                .fiatEquivalentOfSui(fiat.getBalance().divide(exchangeRate, 2, RoundingMode.HALF_UP))
                .suiBalance(sui.getBalance())
                .suiToken("SUI")
                .suiEquivalentOfFiat(sui.getBalance().multiply(exchangeRate))
                .suiToNgnRate(exchangeRate)
                .build();
    }

    @Override
    public List<Wallet> getWallets(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return walletRepository.findByUser(user);
    }

    @Override
    public void depositFiat(UUID userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserIdAndCurrency(userId, WalletCurrency.NGN);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Override
    public void withdrawFiat(UUID userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserIdAndCurrency(userId, WalletCurrency.NGN);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Not enough fiat balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    @Override
    public void depositSui(UUID userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserIdAndCurrency(userId, WalletCurrency.SUI);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    @Override
    public void withdrawSui(UUID userId, BigDecimal amount) {
        Wallet wallet = getWalletByUserIdAndCurrency(userId, WalletCurrency.SUI);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Not enough crypto balance");
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }


    private Wallet getWalletByUserIdAndCurrency(UUID userId, WalletCurrency currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return getWalletByType(user, currency);
    }

    private Wallet getWalletByType(User user, WalletCurrency currency) {
        return walletRepository.findByUserAndCurrencyType(user, currency)
                .orElseThrow(() -> new WalletNotFoundException("Wallet for " + currency + " not found"));
    }

    private CreateWalletResponse mapToCreateWalletResponse(Wallet wallet) {
        return CreateWalletResponse.builder()
                .message("Wallet created successfully")
                .accountNumber(wallet.getAccountNumber())
                .walletCurrency(wallet.getCurrencyType())
                .walletType(wallet.getWalletType())
                .walletStatus(wallet.getStatus())
                .balance(wallet.getBalance())
                .build();
    }
}
