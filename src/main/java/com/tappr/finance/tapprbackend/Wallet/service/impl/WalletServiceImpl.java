package com.tappr.finance.tapprbackend.Wallet.service.impl;

import com.tappr.finance.tapprbackend.Wallet.data.model.Currency;
import com.tappr.finance.tapprbackend.Wallet.data.model.Wallet;
import com.tappr.finance.tapprbackend.Wallet.data.repositories.CurrencyRepository;
import com.tappr.finance.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.tappr.finance.tapprbackend.Wallet.service.interfaces.WalletService;
import com.tappr.finance.tapprbackend.user.data.models.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;
    private final SecureRandom secureRandom = new SecureRandom(); // For NUBAN generation

    @Value("${node_service_url}") // Ensure this matches application.properties
    private String NODE_SERVICE_URL;

    @Transactional
    @Override
    public Wallet createWallet(User user, String currencyCode) {
        // 1. Safety Check (Idempotency at the low level)
        if (walletRepository.findByUserAndCurrencyCode(user, currencyCode).isPresent()) {
            throw new RuntimeException("Wallet already exists for currency: " + currencyCode);
        }

        Currency currency = currencyRepository.findById(currencyCode)
                .orElseThrow(() -> new RuntimeException("Currency not supported: " + currencyCode));

        Wallet.WalletBuilder walletBuilder = Wallet.builder()
                .user(user)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .isOmnibus(false);

        // 2. Handle Currency-Specific Logic
        if ("SUI".equalsIgnoreCase(currency.getCode())) {
            // Option B: HD Wallet Derivation
            Long hdIndex = walletRepository.getNextDerivationIndex();

            // Call Node.js to get public address
            String address = fetchDerivedAddressFromNode(hdIndex);

            walletBuilder.derivationIndex(hdIndex);
            walletBuilder.depositAddress(address);

        } else if ("NGN".equalsIgnoreCase(currency.getCode())) {
            // Generate Virtual Account Number
            walletBuilder.accountNumber(generateNuban());
        }

        return walletRepository.save(walletBuilder.build());
    }

    /**
     * Called automatically after KYC Tier 1 is verified.
     * Ensures the user has the default set of wallets.
     */
    @Override
    @Transactional
    public void createWalletForUser(User user) {
        log.info("Initializing default wallets for User: {}", user.getId());

        // 1. Create NGN Wallet if missing
        if (walletRepository.findByUserAndCurrencyCode(user, "NGN").isEmpty()) {
            try {
                createWallet(user, "NGN");
                log.info("NGN Wallet created for user {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to create NGN wallet", e);
            }
        }

        // 2. Create SUI Wallet if missing
        if (walletRepository.findByUserAndCurrencyCode(user, "SUI").isEmpty()) {
            try {
                createWallet(user, "SUI");
                log.info("SUI Wallet created for user {}", user.getId());
            } catch (Exception e) {
                log.error("Failed to create SUI wallet", e);
            }
        }
    }

    private String fetchDerivedAddressFromNode(Long index) {
        try {
            Map<String, Object> request = Map.of("index", index);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    NODE_SERVICE_URL + "/api/sui/derive-address",
                    request,
                    Map.class
            );

            if (response.getBody() == null || response.getBody().get("address") == null) {
                throw new RuntimeException("Invalid response from Node service");
            }

            return (String) response.getBody().get("address");
        } catch (Exception e) {
            log.error("Error fetching address from Node service", e);
            throw new RuntimeException("Key Management Service Unavailable");
        }
    }

    private String generateNuban() {
        String accountNumber;
        // Loop until we find a unique number (highly unlikely to collide, but safe)
        do {
            // Generate random 10-digit number.
            // 1_000_000_000L ensures it always starts with at least 1 and is 10 digits
            long number = 1_000_000_000L + secureRandom.nextLong(9_000_000_000L);
            accountNumber = String.valueOf(number).substring(0, 10);

        } while (walletRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }
}