package com.semicolon.africa.tapprbackend.user.dtos.responses;

import com.semicolon.africa.tapprbackend.Wallet.dtos.response.WalletBalanceResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginResponseTest {

    @Test
    public void testLoginResponseConstructor() {
        // Test that LoginResponse constructor works with all required parameters
        WalletBalanceResponse walletBalances = WalletBalanceResponse.builder()
                .fiatBalance(java.math.BigDecimal.valueOf(1000))
                .fiatCurrency("NGN")
                .suiBalance(java.math.BigDecimal.valueOf(50))
                .suiToken("SUI")
                .build();

        LoginResponse loginResponse = new LoginResponse(
                "Login successful",
                "access-token-123",
                "refresh-token-456",
                Role.REGULAR,
                true,
                "user-id-789",
                true,
                true,
                "fiat-account-123",
                "sui-address-456",
                walletBalances
        );

        assertNotNull(loginResponse);
        assertEquals("Login successful", loginResponse.getMessage());
        assertEquals("access-token-123", loginResponse.getAccessToken());
        assertEquals("refresh-token-456", loginResponse.getRefreshToken());
        assertEquals(Role.REGULAR, loginResponse.getRole());
        assertTrue(loginResponse.isLoggedIn());
        assertEquals("user-id-789", loginResponse.getUserId());
        assertTrue(loginResponse.isHasSuiWallet());
        assertTrue(loginResponse.isHasFiatWallet());
        assertEquals("fiat-account-123", loginResponse.getFiatWalletAccountNumber());
        assertEquals("sui-address-456", loginResponse.getSuiWalletAddress());
        assertNotNull(loginResponse.getWalletBalances());
    }

    @Test
    public void testLoginResponseConstructorWithNullWalletBalances() {
        // Test that LoginResponse constructor works with null wallet balances
        LoginResponse loginResponse = new LoginResponse(
                "Login successful",
                "access-token-123",
                "refresh-token-456",
                Role.REGULAR,
                true,
                "user-id-789",
                false,
                false,
                null,
                null,
                null
        );

        assertNotNull(loginResponse);
        assertEquals("Login successful", loginResponse.getMessage());
        assertFalse(loginResponse.isHasSuiWallet());
        assertFalse(loginResponse.isHasFiatWallet());
        assertNull(loginResponse.getFiatWalletAccountNumber());
        assertNull(loginResponse.getSuiWalletAddress());
        assertNull(loginResponse.getWalletBalances());
    }
}