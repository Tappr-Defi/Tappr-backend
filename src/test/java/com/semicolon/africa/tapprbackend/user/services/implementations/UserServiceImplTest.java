package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.Wallet.dtos.requests.CreateWalletRequest;
import com.semicolon.africa.tapprbackend.Wallet.service.interfaces.WalletService;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import com.semicolon.africa.tapprbackend.user.services.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;
    private CreateNewUserRequest createNewUserRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        String rawPassword = "securePassword123";

        createNewUserRequest = new CreateNewUserRequest();
        createNewUserRequest.setFirstName("JohnDaniel");
        createNewUserRequest.setLastName("Ike");
        createNewUserRequest.setEmail("john.daniel@gmail.com");
        createNewUserRequest.setPassword(rawPassword);
        createNewUserRequest.setPhoneNumber("+2348123456789");
    }

//    @Test
//    public void testThatOnSignUpARegisteredUserAlsoHasAWallet_generationIsSuccessful() {
//        CreateNewUserResponse response = authService.createNewUser(createNewUserRequest);
//        assertNotNull(response);
//
//        User user = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
//        assertNotNull(user);
//        assertEquals(Role.REGULAR,user.getRole());
//        assertEquals(createNewUserRequest.getFirstName(),user.getFirstName());
//
//        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
//        createWalletRequest.setUser(user.getId());
//        createWalletRequest.setType(WalletType.SAVINGS);
//
//    }

}