
package com.semicolon.africa.tapprbackend.Wallet.service.implementation;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.semicolon.africa.tapprbackend.Wallet.dtos.requests.CreateWalletRequest;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.CreateWalletResponse;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletStatus;
import com.semicolon.africa.tapprbackend.Wallet.exceptions.WalletAlreadyExistsException;
import com.semicolon.africa.tapprbackend.Wallet.service.interfaces.WalletService;
import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.exceptions.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class WalletServiceImpl implements WalletService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WalletRepository walletRepository;

    public WalletServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.walletRepository = walletRepository;
    }

    @Override
    public CreateWalletResponse createWalletForUser(String jwtToken, CreateWalletRequest createWalletRequest) {
        String email = jwtUtil.extractEmail(jwtToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (walletRepository.existsByUser(user)) {
            throw new IllegalStateException("User already has a wallet");
        }

        Wallet wallet = mapNewWalletCreationToUser(createWalletRequest, user);
        return mapToCreateWalletResponse(wallet);
    }

    @Override
    public void createWalletIfNotExists(User user) {
        if (walletRepository.existsByUser(user)) {
            log.info("Wallet already exists for user: {}", user.getEmail());
            return;
        }

        WalletCurrency currency = resolveCurrencyFromPhoneNumber(user.getPhoneNumber());
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrencyType(currency);
        wallet.setWalletType(currency.getWalletType());
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setAccountNumber(generateAccountNumberFromPhoneNumber(user));

        walletRepository.save(wallet);
        
        // Update the user with the wallet reference
        user.setWallet(wallet);
        userRepository.save(user);
        
        log.info("Wallet created for user {} with currency {}", user.getEmail(), currency);
    }

    @Override
    public CreateWalletResponse createWallet(User user) {
        if (walletRepository.existsByUser(user)) {
            log.info("Wallet already exists for user: {}", user.getEmail());
            throw new WalletAlreadyExistsException("User already has a wallet");
        }

        WalletCurrency currency = resolveCurrencyFromPhoneNumber(user.getPhoneNumber());
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrencyType(currency);
        wallet.setWalletType(currency.getWalletType());
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setAccountNumber(generateAccountNumberFromPhoneNumber(user));

        walletRepository.save(wallet);
        user.setWallet(wallet);
        userRepository.save(user);

        log.info("Wallet created for user {} with currency {}", user.getEmail(), currency);
        return mapToCreateWalletResponse(wallet);
    }


    private Wallet mapNewWalletCreationToUser(CreateWalletRequest createWalletRequest, User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setWalletType(createWalletRequest.getType());
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setAccountNumber(generateAccountNumberFromPhoneNumber(user));

        String countryCode = getCountryCodeFromPhoneNumber(user.getPhoneNumber());
        WalletCurrency currency = resolveCurrencyFromCountry(countryCode);
        wallet.setCurrencyType(currency);

        return walletRepository.save(wallet);
    }

    private String generateAccountNumberFromPhoneNumber(User user) {
        String phoneNumber = user.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.trim().isEmpty())
            throw new IllegalArgumentException("User phone number cannot be null or empty");
        String cleanPhoneNumber = phoneNumber.replaceAll("\\D", "");
        if (cleanPhoneNumber.isEmpty())
            return String.valueOf(System.currentTimeMillis()).substring(3);
        if (cleanPhoneNumber.length() > 10)
            return cleanPhoneNumber.substring(cleanPhoneNumber.length() - 10);
        if (cleanPhoneNumber.length() < 10) {
            try {
                return String.format("%010d", Long.parseLong(cleanPhoneNumber));
            } catch (NumberFormatException e) {
                return String.valueOf(System.currentTimeMillis()).substring(3);
            }
        }

        return cleanPhoneNumber;
    }

    private String getCountryCodeFromPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "NG";
        }

        try {
            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
            Phonenumber.PhoneNumber parsedNumber = phoneUtil.parse(phoneNumber, null);

            if (phoneUtil.isValidNumber(parsedNumber)) {
                String regionCode = phoneUtil.getRegionCodeForNumber(parsedNumber);
                return regionCode != null ? regionCode : "NG";
            } else {
                return "NG";
            }
        } catch (Exception exception) {
            log.error("Error resolving country code from phone number: {}", exception.getMessage());
            return "NG";
        }
    }

//    public WalletCurrency resolveCurrencyFromPhoneNumber(String phoneNumber) {
//        String countryCode = getCountryCodeFromPhoneNumber(phoneNumber);
//        return resolveCurrencyFromCountry(countryCode);
//    }

    public WalletCurrency resolveCurrencyFromPhoneNumber(String phoneNumber) {
        String countryCode = getCountryCodeFromPhoneNumber(phoneNumber);
//        return CurrencyResolver.resolveCurrencyFromCountry(countryCode);
        return resolveCurrencyFromCountry(countryCode);
    }



    private WalletCurrency resolveCurrencyFromCountry(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return WalletCurrency.NGN;
        }

        return switch (countryCode.toUpperCase()) {
            // Major African countries
            case "NG" -> WalletCurrency.NGN; // Nigeria
            case "KE" -> WalletCurrency.KES; // Kenya
            case "GH" -> WalletCurrency.GHS; // Ghana
            case "ZA" -> WalletCurrency.ZAR; // South Africa
            case "EG" -> WalletCurrency.EGP; // Egypt
            case "DZ" -> WalletCurrency.DZD; // Algeria
            case "MA" -> WalletCurrency.MAD; // Morocco
            case "UG" -> WalletCurrency.UGX; // Uganda
            case "TZ" -> WalletCurrency.TZS; // Tanzania
            case "ET" -> WalletCurrency.ETB; // Ethiopia
            case "RW" -> WalletCurrency.RWF; // Rwanda
            case "BW" -> WalletCurrency.BWP; // Botswana
            case "MZ" -> WalletCurrency.MZN; // Mozambique
            case "SD" -> WalletCurrency.SDG; // Sudan

            // CFA Franc zones
            // Senegal
            // Ivory Coast
            // Mali
            // Niger
            // Burkina Faso
            // Togo
            // Benin
            case "SN", "CI", "ML", "NE", "BF", "TG", "BJ", "GW" -> // Guinea-Bissau
                    WalletCurrency.XOF; // West African CFA franc

            // Cameroon
            // Central African Republic
            // Gabon
            // Republic of the Congo
            // Chad
            case "CM", "CF", "GA", "CG", "TD", "GQ" -> // Equatorial Guinea
                    WalletCurrency.XAF; // Central African CFA franc

            // Major global countries
            case "US" -> WalletCurrency.USD; // United States
            case "GB" -> WalletCurrency.GBP; // United Kingdom
            // Germany
            // France
            // Italy
            case "EU", "DE", "FR", "IT", "ES" -> // Spain
                    WalletCurrency.EUR; // European Union
            case "IN" -> WalletCurrency.INR; // India
            case "JP" -> WalletCurrency.JPY; // Japan
            case "AE" -> WalletCurrency.AED; // UAE

            default -> WalletCurrency.NGN;
        };
    }


    private CreateWalletResponse mapToCreateWalletResponse(Wallet wallet) {
        CreateWalletResponse response = new CreateWalletResponse();
        response.setMessage("Wallet created successfully");
        response.setAccountNumber(wallet.getAccountNumber());
        response.setWalletCurrency(wallet.getCurrencyType());
        response.setWalletStatus(wallet.getStatus());
        response.setWalletType(wallet.getWalletType());
        response.setBalance(wallet.getBalance());
        return response;
    }
}






