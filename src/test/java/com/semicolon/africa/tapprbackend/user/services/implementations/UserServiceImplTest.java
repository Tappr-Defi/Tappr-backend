package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.Wallet.data.model.Wallet;
import com.semicolon.africa.tapprbackend.Wallet.data.repositories.WalletRepository;
import com.semicolon.africa.tapprbackend.Wallet.dtos.requests.CreateWalletRequest;
import com.semicolon.africa.tapprbackend.Wallet.dtos.response.WalletBalanceResponse;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletCurrency;
import com.semicolon.africa.tapprbackend.Wallet.enums.WalletType;
import com.semicolon.africa.tapprbackend.Wallet.service.interfaces.WalletService;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.exceptions.UserNotFoundException;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import com.semicolon.africa.tapprbackend.user.services.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("UserService Implementation Tests")
public class UserServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;

    private CreateNewUserRequest createNewUserRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        walletRepository.deleteAll();
        
        String rawPassword = "securePassword123";

        createNewUserRequest = new CreateNewUserRequest();
        createNewUserRequest.setFirstName("JohnDaniel");
        createNewUserRequest.setLastName("Ike");
        createNewUserRequest.setEmail("john.daniel@gmail.com");
        createNewUserRequest.setPassword(rawPassword);
        createNewUserRequest.setPhoneNumber("+2348123456789");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.daniel@gmail.com");
        loginRequest.setPassword(rawPassword);
    }

    @Test
    @DisplayName("Should find user by email successfully")
    public void shouldFindUserByEmailSuccessfully() {
        // Create a user first
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        assertNotNull(response);

        // Test finding user by email
        Optional<User> foundUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase());
        assertTrue(foundUser.isPresent());
        assertEquals(createNewUserRequest.getEmail().toLowerCase(), foundUser.get().getEmail());
        assertEquals(createNewUserRequest.getFirstName(), foundUser.get().getFirstName());
        assertEquals(createNewUserRequest.getLastName(), foundUser.get().getLastName());
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    public void shouldReturnEmptyWhenUserNotFoundByEmail() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Should check if user exists by email")
    public void shouldCheckIfUserExistsByEmail() {
        // Initially user should not exist
        assertFalse(userRepository.existsByEmail(createNewUserRequest.getEmail()));

        // Create user
        authService.createNewUser(createNewUserRequest);

        // Now user should exist
        assertTrue(userRepository.existsByEmail(createNewUserRequest.getEmail().toLowerCase()));
    }

    @Test
    @DisplayName("Should check if user exists by phone number")
    public void shouldCheckIfUserExistsByPhoneNumber() {
        // Initially user should not exist
        assertFalse(userRepository.existsByPhoneNumber(createNewUserRequest.getPhoneNumber()));

        // Create user
        authService.createNewUser(createNewUserRequest);

        // Now user should exist
        assertTrue(userRepository.existsByPhoneNumber(createNewUserRequest.getPhoneNumber()));
    }

    @Test
    @DisplayName("Should handle case insensitive email search")
    public void shouldHandleCaseInsensitiveEmailSearch() {
        authService.createNewUser(createNewUserRequest);

        // Test with different cases
        assertTrue(userRepository.existsByEmail(createNewUserRequest.getEmail().toUpperCase()));
        assertTrue(userRepository.existsByEmail(createNewUserRequest.getEmail().toLowerCase()));
        assertTrue(userRepository.existsByEmail("JOHN.DANIEL@GMAIL.COM"));
    }

    @Test
    @DisplayName("Should save user with correct default values")
    public void shouldSaveUserWithCorrectDefaultValues() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        // Check default values
        assertEquals(Role.REGULAR, savedUser.getRole());
        assertFalse(savedUser.isKycVerified());
        assertFalse(savedUser.isLoggedIn());
        assertNull(savedUser.getLastLoginAt());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getId());
        assertTrue(savedUser.isHasWallet());
    }

    @Test
    @DisplayName("Should create wallets automatically on user registration")
    public void shouldCreateWalletsAutomaticallyOnUserRegistration() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        // Check that wallets were created
        List<Wallet> userWallets = walletRepository.findByUser(savedUser);
        assertNotNull(userWallets);
        assertEquals(2, userWallets.size()); // Should have both fiat and crypto wallets
        
        // Check for NGN wallet
        boolean hasNgnWallet = userWallets.stream()
                .anyMatch(wallet -> wallet.getCurrencyType() == WalletCurrency.NGN);
        assertTrue(hasNgnWallet);
        
        // Check for SUI wallet
        boolean hasSuiWallet = userWallets.stream()
                .anyMatch(wallet -> wallet.getCurrencyType() == WalletCurrency.SUI);
        assertTrue(hasSuiWallet);
    }

    @Test
    @DisplayName("Should get wallet balances for user")
    public void shouldGetWalletBalancesForUser() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        WalletBalanceResponse balances = walletService.getUserWalletBalances(savedUser.getId());
        
        assertNotNull(balances);
        assertNotNull(balances.getFiatBalance());
        assertNotNull(balances.getSuiBalance());
        assertEquals("NGN", balances.getFiatCurrency());
        assertEquals("SUI", balances.getSuiToken());
        assertEquals(BigDecimal.ZERO, balances.getFiatBalance());
        assertEquals(BigDecimal.ZERO, balances.getSuiBalance());
    }

    @Test
    @DisplayName("Should throw exception when getting balances for non-existent user")
    public void shouldThrowExceptionWhenGettingBalancesForNonExistentUser() {
        UUID nonExistentUserId = UUID.randomUUID();
        
        assertThrows(UserNotFoundException.class, () -> {
            walletService.getUserWalletBalances(nonExistentUserId);
        });
    }

    @Test
    @DisplayName("Should deposit fiat successfully")
    public void shouldDepositFiatSuccessfully() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        BigDecimal depositAmount = BigDecimal.valueOf(1000);
        walletService.depositFiat(savedUser.getId(), depositAmount);
        
        WalletBalanceResponse balances = walletService.getUserWalletBalances(savedUser.getId());
        assertEquals(depositAmount, balances.getFiatBalance());
    }

    @Test
    @DisplayName("Should withdraw fiat successfully")
    public void shouldWithdrawFiatSuccessfully() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        // First deposit some money
        BigDecimal depositAmount = BigDecimal.valueOf(1000);
        walletService.depositFiat(savedUser.getId(), depositAmount);
        
        // Then withdraw some
        BigDecimal withdrawAmount = BigDecimal.valueOf(300);
        walletService.withdrawFiat(savedUser.getId(), withdrawAmount);
        
        WalletBalanceResponse balances = walletService.getUserWalletBalances(savedUser.getId());
        assertEquals(BigDecimal.valueOf(700), balances.getFiatBalance());
    }

    @Test
    @DisplayName("Should deposit SUI successfully")
    public void shouldDepositSuiSuccessfully() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        BigDecimal depositAmount = BigDecimal.valueOf(50);
        walletService.depositSui(savedUser.getId(), depositAmount);
        
        WalletBalanceResponse balances = walletService.getUserWalletBalances(savedUser.getId());
        assertEquals(depositAmount, balances.getSuiBalance());
    }

    @Test
    @DisplayName("Should withdraw SUI successfully")
    public void shouldWithdrawSuiSuccessfully() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        // First deposit some SUI
        BigDecimal depositAmount = BigDecimal.valueOf(50);
        walletService.depositSui(savedUser.getId(), depositAmount);
        
        // Then withdraw some
        BigDecimal withdrawAmount = BigDecimal.valueOf(20);
        walletService.withdrawSui(savedUser.getId(), withdrawAmount);
        
        WalletBalanceResponse balances = walletService.getUserWalletBalances(savedUser.getId());
        assertEquals(BigDecimal.valueOf(30), balances.getSuiBalance());
    }

    @Test
    @DisplayName("Should throw exception when withdrawing more than balance")
    public void shouldThrowExceptionWhenWithdrawingMoreThanBalance() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        // Try to withdraw without having any balance
        BigDecimal withdrawAmount = BigDecimal.valueOf(100);
        
        assertThrows(Exception.class, () -> {
            walletService.withdrawFiat(savedUser.getId(), withdrawAmount);
        });
    }

    @Test
    @DisplayName("Should handle multiple users with different wallets")
    public void shouldHandleMultipleUsersWithDifferentWallets() {
        // Create first user
        CreateNewUserResponse response1 = authService.createNewUser(createNewUserRequest);
        User user1 = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        // Create second user
        CreateNewUserRequest secondUserRequest = new CreateNewUserRequest();
        secondUserRequest.setFirstName("Jane");
        secondUserRequest.setLastName("Doe");
        secondUserRequest.setEmail("jane.doe@gmail.com");
        secondUserRequest.setPassword("anotherPassword123");
        secondUserRequest.setPhoneNumber("+2348987654321");
        
        CreateNewUserResponse response2 = authService.createNewUser(secondUserRequest);
        User user2 = userRepository.findByEmail(secondUserRequest.getEmail().toLowerCase()).get();
        
        // Deposit different amounts for each user
        walletService.depositFiat(user1.getId(), BigDecimal.valueOf(1000));
        walletService.depositFiat(user2.getId(), BigDecimal.valueOf(2000));
        
        // Check balances are separate
        WalletBalanceResponse balances1 = walletService.getUserWalletBalances(user1.getId());
        WalletBalanceResponse balances2 = walletService.getUserWalletBalances(user2.getId());
        
        assertEquals(BigDecimal.valueOf(1000), balances1.getFiatBalance());
        assertEquals(BigDecimal.valueOf(2000), balances2.getFiatBalance());
    }

    @Test
    @DisplayName("Should get user wallets list")
    public void shouldGetUserWalletsList() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        List<Wallet> wallets = walletService.getWallets(savedUser.getId());
        
        assertNotNull(wallets);
        assertEquals(2, wallets.size()); // Should have both fiat and crypto wallets
        
        // Verify wallet types
        boolean hasFiatWallet = wallets.stream()
                .anyMatch(wallet -> wallet.getWalletType() == WalletType.FIAT);
        boolean hasCryptoWallet = wallets.stream()
                .anyMatch(wallet -> wallet.getWalletType() == WalletType.CRYPTO);
        
        assertTrue(hasFiatWallet);
        assertTrue(hasCryptoWallet);
    }

    @Test
    @DisplayName("Should handle user login and wallet association")
    public void shouldHandleUserLoginAndWalletAssociation() {
        // Create user
        CreateNewUserResponse signupResponse = authService.createNewUser(createNewUserRequest);
        
        // Login user
        LoginResponse loginResponse = authService.login(loginRequest);
        
        assertNotNull(loginResponse);
        assertTrue(loginResponse.isLoggedIn());
        assertTrue(loginResponse.isHasFiatWallet());
        assertTrue(loginResponse.isHasSuiWallet());
        assertNotNull(loginResponse.getFiatWalletAccountNumber());
        assertNotNull(loginResponse.getSuiWalletAddress());
        assertNotNull(loginResponse.getWalletBalances());
    }

    @Test
    @DisplayName("Should update user login status correctly")
    public void shouldUpdateUserLoginStatusCorrectly() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        
        // Initially user should not be logged in
        User userBeforeLogin = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertFalse(userBeforeLogin.isLoggedIn());
        
        // Login user
        authService.login(loginRequest);
        
        // After login, user should be logged in
        User userAfterLogin = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertTrue(userAfterLogin.isLoggedIn());
        assertNotNull(userAfterLogin.getLastLoginAt());
    }

    @Test
    @DisplayName("Should handle concurrent wallet operations")
    public void shouldHandleConcurrentWalletOperations() {
        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        
        // Perform multiple operations
        walletService.depositFiat(savedUser.getId(), BigDecimal.valueOf(1000));
        walletService.depositSui(savedUser.getId(), BigDecimal.valueOf(50));
        walletService.withdrawFiat(savedUser.getId(), BigDecimal.valueOf(200));
        walletService.withdrawSui(savedUser.getId(), BigDecimal.valueOf(10));
        
        WalletBalanceResponse balances = walletService.getUserWalletBalances(savedUser.getId());
        assertEquals(BigDecimal.valueOf(800), balances.getFiatBalance());
        assertEquals(BigDecimal.valueOf(40), balances.getSuiBalance());
    }

}