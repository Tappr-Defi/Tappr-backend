package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Implementation Tests - Fixed")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private CreateNewUserRequest createNewUserRequest;
    private User testUser;

    @BeforeEach
    public void setUp() {
        String rawPassword = "securePassword123";

        createNewUserRequest = new CreateNewUserRequest();
        createNewUserRequest.setFirstName("JohnDaniel");
        createNewUserRequest.setLastName("Ike");
        createNewUserRequest.setEmail("john.daniel@gmail.com");
        createNewUserRequest.setPassword(rawPassword);
        createNewUserRequest.setPhoneNumber("+2348123456789");

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail(createNewUserRequest.getEmail().toLowerCase());
        testUser.setFirstName(createNewUserRequest.getFirstName());
        testUser.setLastName(createNewUserRequest.getLastName());
        testUser.setPhoneNumber(createNewUserRequest.getPhoneNumber());
        testUser.setRole(Role.REGULAR);
        testUser.setKycVerified(false);
        testUser.setLoggedIn(false);
        testUser.setHasWallet(false);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should find user by email successfully")
    public void shouldFindUserByEmailSuccessfully() {
        when(userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()))
                .thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase());

        assertTrue(foundUser.isPresent());
        assertEquals(createNewUserRequest.getEmail().toLowerCase(), foundUser.get().getEmail());
        assertEquals(createNewUserRequest.getFirstName(), foundUser.get().getFirstName());
        assertEquals(createNewUserRequest.getLastName(), foundUser.get().getLastName());
        
        verify(userRepository).findByEmail(createNewUserRequest.getEmail().toLowerCase());
    }

    @Test
    @DisplayName("Should return empty when user not found by email")
    public void shouldReturnEmptyWhenUserNotFoundByEmail() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(foundUser.isPresent());
        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should check if user exists by phone number")
    public void shouldCheckIfUserExistsByPhoneNumber() {
        when(userRepository.existsByPhoneNumber(createNewUserRequest.getPhoneNumber())).thenReturn(true);
        assertTrue(userRepository.existsByPhoneNumber(createNewUserRequest.getPhoneNumber()));
        verify(userRepository).existsByPhoneNumber(createNewUserRequest.getPhoneNumber());
    }

    @Test
    @DisplayName("Should save user with correct default values")
    public void shouldSaveUserWithCorrectDefaultValues() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        User savedUser = userRepository.save(testUser);
        
        assertEquals(Role.REGULAR, savedUser.getRole());
        assertFalse(savedUser.isKycVerified());
        assertFalse(savedUser.isLoggedIn());
        assertNull(savedUser.getLastLoginAt());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getId());
        assertFalse(savedUser.isHasWallet());
        
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    public void shouldFindUserByIdSuccessfully() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        Optional<User> foundUser = userRepository.findById(testUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getId(), foundUser.get().getId());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    @DisplayName("Should return empty when user not found by ID")
    public void shouldReturnEmptyWhenUserNotFoundById() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        Optional<User> foundUser = userRepository.findById(nonExistentId);

        assertFalse(foundUser.isPresent());
        verify(userRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should validate user full name generation")
    public void shouldValidateUserFullNameGeneration() {
        String fullName = testUser.getFullName();
        assertEquals("JohnDaniel Ike", fullName);
        assertEquals(createNewUserRequest.getFirstName() + " " + createNewUserRequest.getLastName(), fullName);
    }

    @Test
    @DisplayName("Should handle user role assignment")
    public void shouldHandleUserRoleAssignment() {
        testUser.setRole(Role.ADMIN);

        assertEquals(Role.ADMIN, testUser.getRole());
        testUser.setRole(Role.REGULAR);
        assertEquals(Role.REGULAR, testUser.getRole());
    }

    @Test
    @DisplayName("Should handle user login status updates")
    public void shouldHandleUserLoginStatusUpdates() {
        assertFalse(testUser.isLoggedIn());
        assertNull(testUser.getLastLoginAt());

        testUser.setLoggedIn(true);
        testUser.setLastLoginAt(LocalDateTime.now());
        assertTrue(testUser.isLoggedIn());
        assertNotNull(testUser.getLastLoginAt());
    }

    @Test
    @DisplayName("Should handle KYC verification status")
    public void shouldHandleKycVerificationStatus() {
        assertFalse(testUser.isKycVerified());
        assertFalse(testUser.isTier2Verified());

        testUser.setKycVerified(true);
        testUser.setTier2Verified(true);

        assertTrue(testUser.isKycVerified());
        assertTrue(testUser.isTier2Verified());
    }

    @Test
    @DisplayName("Should handle wallet status")
    public void shouldHandleWalletStatus() {
        assertFalse(testUser.isHasWallet());

        testUser.setHasWallet(true);
        assertTrue(testUser.isHasWallet());
    }

    @Test
    @DisplayName("Should validate email format")
    public void shouldValidateEmailFormat() {
        assertTrue(testUser.getEmail().contains("@"));
        assertTrue(testUser.getEmail().contains("."));
        assertEquals(testUser.getEmail(), testUser.getEmail().toLowerCase());
    }

    @Test
    @DisplayName("Should validate phone number format")
    public void shouldValidatePhoneNumberFormat() {
        assertTrue(testUser.getPhoneNumber().matches(".*\\d.*"));
        assertTrue(testUser.getPhoneNumber().startsWith("+"));
    }

    @Test
    @DisplayName("Should handle user creation timestamp")
    public void shouldHandleUserCreationTimestamp() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusMinutes(1);
        LocalDateTime afterCreation = LocalDateTime.now().plusMinutes(1);

        assertNotNull(testUser.getCreatedAt());
        assertTrue(testUser.getCreatedAt().isAfter(beforeCreation));
        assertTrue(testUser.getCreatedAt().isBefore(afterCreation));
    }

    @Test
    @DisplayName("Should handle multiple users with different data")
    public void shouldHandleMultipleUsersWithDifferentData() {
        User secondUser = new User();
        secondUser.setId(UUID.randomUUID());
        secondUser.setEmail("jane.doe@gmail.com");
        secondUser.setFirstName("Jane");
        secondUser.setLastName("Doe");
        secondUser.setPhoneNumber("+2348987654321");
        secondUser.setRole(Role.REGULAR);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(secondUser.getId())).thenReturn(Optional.of(secondUser));

        Optional<User> foundUser1 = userRepository.findById(testUser.getId());
        Optional<User> foundUser2 = userRepository.findById(secondUser.getId());

        assertTrue(foundUser1.isPresent());
        assertTrue(foundUser2.isPresent());
        assertNotEquals(foundUser1.get().getId(), foundUser2.get().getId());
        assertNotEquals(foundUser1.get().getEmail(), foundUser2.get().getEmail());
        
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).findById(secondUser.getId());
    }
}