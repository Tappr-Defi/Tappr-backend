package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
import com.semicolon.africa.tapprbackend.user.dtos.requests.LogoutRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.exceptions.UserNotFoundException;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuthServiceImplTest {


    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authenticationService;

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

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john.daniel@gmail.com");
        loginRequest.setPassword(rawPassword);

    }

    @Test
    public void test_thatUserCanSignUp_shouldSaveUser() {
       CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);

        User user = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertNotNull(user);
        assertEquals(Role.REGULAR,user.getRole());
        assertEquals(createNewUserRequest.getFirstName(),user.getFirstName());
    }

    @Test
    public void testSignUpFailsWithInvalidEmail() {
        createNewUserRequest.setEmail("invalid-email-format");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithEmptyPassword() {
        createNewUserRequest.setPassword("");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testJwtTokenIsGeneratedOnLogin() {
        authenticationService.createNewUser(createNewUserRequest);
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(createNewUserRequest.getEmail());
        loginRequest.setPassword(createNewUserRequest.getPassword());
        
        LoginResponse loginResponse = authenticationService.login(loginRequest);
        
        assertNotNull(loginResponse.getAccessToken());
        assertFalse(loginResponse.getAccessToken().isEmpty());
    }

//    @Test
//    public void testSignUpFailsWithInvalidSpecialCharactersInName() {
//        createNewUserRequest.setFirstName("John@Daniel");
//
//        assertThrows(IllegalArgumentException.class, () -> {
//            authenticationService.createNewUser(createNewUserRequest);
//        });
//    }

    @Test
    public void testSignUpFailsWhenUserAlreadyExists() {
        authenticationService.createNewUser(createNewUserRequest);
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithNullFields() {
        createNewUserRequest.setFirstName(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void test_registeredUserCanSuccessfullyLogIn_shouldReturnTrue() {
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertEquals(response.getMessage(),"User created successfully");

        LoginResponse loginResponse = authenticationService.login(loginRequest);
        assertEquals(loginResponse.getMessage(),"Logged in successfully");
        assertTrue(loginResponse.isLoggedIn());
    }

    @Test
    public void testSignUpSucceedsWithHyphenInFirstName() {
        createNewUserRequest.setFirstName("Mary-Jane");
        
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
        
        User user = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertEquals("Mary-Jane", user.getFirstName());
    }

    @Test
    public void testSignUpSucceedsWithHyphenInLastName() {
        createNewUserRequest.setLastName("Smith-Jones");
        
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
        
        User user = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertEquals("Smith-Jones", user.getLastName());
    }

    @Test
    public void testSignUpSucceedsWithApostropheInName() {
        createNewUserRequest.setFirstName("O'Connor");
        
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
        
        User user = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertEquals("O'Connor", user.getFirstName());
    }

    @Test
    public void testSignUpSucceedsWithHyphenInEmail() {
        createNewUserRequest.setEmail("mary-jane@example.com");
        
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
        
        User user = userRepository.findByEmail("mary-jane@example.com").get();
        assertEquals("mary-jane@example.com", user.getEmail());
    }

    @Test
    public void testSignUpSucceedsWithUnderscoreInEmail() {
        createNewUserRequest.setEmail("john_doe@example.com");
        
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
    }

    @Test
    public void testSignUpSucceedsWithPlusInEmail() {
        createNewUserRequest.setEmail("john+test@example.com");
        
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
    }

    @Test
    public void testSignUpSucceedsWithAccentedCharactersInName() {
        createNewUserRequest.setFirstName("José");
        createNewUserRequest.setLastName("Martínez");
        
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
        
        User user = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertEquals("José", user.getFirstName());
        assertEquals("Martínez", user.getLastName());
    }

    @Test
    public void testSignUpPassesWithWhitespaceInFirstName() {
        createNewUserRequest.setFirstName("John Daniel");
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
    }

    @Test
    public void testSignUpPassesWithWhitespaceInLastName() {
        createNewUserRequest.setLastName("Van Halen");
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
    }

    @Test
    public void testSignUpFailsWithInvalidEmailFormat() {
        createNewUserRequest.setEmail("john.daniel.gmail.com"); // Missing @
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithWhitespaceInPassword() {
        createNewUserRequest.setPassword("secure password");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithNullLastName() {
        createNewUserRequest.setLastName(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithNullEmail() {
        createNewUserRequest.setEmail(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithNullPassword() {
        createNewUserRequest.setPassword(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithNullPhoneNumber() {
        createNewUserRequest.setPhoneNumber(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithEmptyFirstName() {
        createNewUserRequest.setFirstName("");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithEmptyLastName() {
        createNewUserRequest.setLastName("");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    public void testSignUpFailsWithEmptyEmail() {
        createNewUserRequest.setEmail("");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

//    @Test
//    public void testSignUpFailsWithInvalidSpecialCharactersInLastName() {
//        createNewUserRequest.setLastName("Ike@123");
//
//        assertThrows(IllegalArgumentException.class, () -> {
//            authenticationService.createNewUser(createNewUserRequest);
//        });
//    }

    @Test
    public void testSuccessfulSignUpCreatesUserWithCorrectDefaults() {
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        
        User user = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertNotNull(user);
        assertEquals(Role.REGULAR, user.getRole());
        assertFalse(user.isKycVerified());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getPasswordHash());
        assertNotEquals(createNewUserRequest.getPassword(), user.getPasswordHash()); // Password should be hashed
    }

    @Test
    public void testSignUpReturnsValidResponse() {
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
        assertNotNull(response.getUserId());
        assertNotNull(response.getEmail());
        assertNotNull(response.getPhoneNumber());
    }
    @Test
    public void testLoginFailsWithNonExistentEmail() {
        loginRequest.setEmail("nonexistent@gmail.com");
        
        UserNotFoundException userNotFoundException = assertThrows(UserNotFoundException.class, () -> {
            authenticationService.login(loginRequest);
        });
        assertEquals("User with that email doesn't exist", userNotFoundException.getMessage());
    }

    @Test
    public void testLoginFailsWithIncorrectPassword() {
        authenticationService.createNewUser(createNewUserRequest);
        
        loginRequest.setPassword("wrongPassword");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.login(loginRequest);
        });
    }

    @Test
    public void testSuccessfulLoginReturnsValidResponse() {
        authenticationService.createNewUser(createNewUserRequest);

        LoginResponse loginResponse = authenticationService.login(loginRequest);

        assertNotNull(loginResponse);
        assertEquals("Logged in successfully", loginResponse.getMessage()); // Ensure this is the message field
        assertTrue(loginResponse.isLoggedIn());
        assertNotNull(loginResponse.getAccessToken());
        assertFalse(loginResponse.getAccessToken().isEmpty());
        assertNotNull(loginResponse.getUserId());
        assertEquals(Role.REGULAR, loginResponse.getRole());
    }


    @Test
    public void testUserIdIsConsistentBetweenSignupAndLogin() {
        CreateNewUserResponse signupResponse = authenticationService.createNewUser(createNewUserRequest);
        
        LoginResponse loginResponse = authenticationService.login(loginRequest);

        assertNotNull(signupResponse.getUserId());
        assertNotNull(loginResponse.getAccessToken());
        assertFalse(loginResponse.getAccessToken().isEmpty());
        
        assertEquals(signupResponse.getUserId(), loginResponse.getUserId());
    }

    @Test
    public void testPasswordIsHashedInDatabase() {
        String rawPassword = "testPassword123";
        createNewUserRequest.setPassword(rawPassword);
        
        authenticationService.createNewUser(createNewUserRequest);
        
        User savedUser = userRepository.findByEmail(createNewUserRequest.getEmail().toLowerCase()).get();
        assertNotEquals(rawPassword, savedUser.getPasswordHash());
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPasswordHash()));
    }

    @Test
    public void testMultipleUsersCanRegisterWithDifferentEmails() {
        authenticationService.createNewUser(createNewUserRequest);
        
        CreateNewUserRequest secondUserRequest = new CreateNewUserRequest();
        secondUserRequest.setFirstName("Jane");
        secondUserRequest.setLastName("Doe");
        secondUserRequest.setEmail("jane.doe@gmail.com");
        secondUserRequest.setPassword("anotherPassword123");
        secondUserRequest.setPhoneNumber("+2348987654321");
        
        CreateNewUserResponse secondResponse = authenticationService.createNewUser(secondUserRequest);
        
        assertNotNull(secondResponse);
        assertEquals("User created successfully", secondResponse.getMessage());
        
        assertTrue(userRepository.findByEmail("john.daniel@gmail.com").isPresent());
        assertTrue(userRepository.findByEmail("jane.doe@gmail.com").isPresent());
        assertEquals(2, userRepository.count());
    }

    @Test
    public void testCaseSensitiveEmailHandling_stillPassesSuccessfully() {
        authenticationService.createNewUser(createNewUserRequest);
        
        loginRequest.setEmail("JOHN.DANIEL@GMAIL.COM");
        assertEquals("Logged in successfully", authenticationService.login(loginRequest).getMessage());
    }

    @Test
    public void testComplexValidNamesAndEmails() {
        CreateNewUserRequest complexUserRequest = new CreateNewUserRequest();
        complexUserRequest.setFirstName("Marie-Claire");
        complexUserRequest.setLastName("O'Brien-Smith");
        complexUserRequest.setEmail("marie-claire.o'brien+test@sub-domain.example.com");
        complexUserRequest.setPassword("complexPassword123");
        complexUserRequest.setPhoneNumber("+1234567890");
        
        CreateNewUserResponse response = authenticationService.createNewUser(complexUserRequest);
        
        assertNotNull(response);
        assertEquals("User created successfully", response.getMessage());
        
        User user = userRepository.findByEmail(complexUserRequest.getEmail().toLowerCase()).get();
        assertEquals("Marie-Claire", user.getFirstName());
        assertEquals("O'Brien-Smith", user.getLastName());
        assertEquals(complexUserRequest.getEmail().toLowerCase(), user.getEmail());
    }

    @Test
    public void testSignUpFailsWithDuplicateEmail() {
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertEquals("User created successfully", response.getMessage());

        CreateNewUserRequest createNewUserRequestWithSameEmail = new CreateNewUserRequest();
        createNewUserRequestWithSameEmail.setFirstName("JohnDaniel");
        createNewUserRequestWithSameEmail.setLastName("Ike");
        createNewUserRequestWithSameEmail.setEmail("john.daniel@gmail.com");
        createNewUserRequestWithSameEmail.setPassword("password");
        createNewUserRequestWithSameEmail.setPhoneNumber("+2348123456789");

        assertEquals("User with this email already exists",
                assertThrows(IllegalArgumentException.class,
                        () -> authenticationService.createNewUser(createNewUserRequestWithSameEmail)).getMessage());
    }

    @Test
    public void testThatLoggedInUserCanLoginFailsForNonRegisteredUser() {
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            authenticationService.login(new LoginRequest());
        });
        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    public void testThatLoggedInUserCanLogOut_flagsUserAsLoggedOut() {
        authenticationService.createNewUser(createNewUserRequest);
        LoginResponse loginResponse = authenticationService.login(loginRequest);
        assertTrue(loginResponse.isLoggedIn());

        LogoutRequest logOutRequest = new LogoutRequest();
        logOutRequest.setEmail(loginRequest.getEmail());
        LogoutUserResponse logoutUserResponse = authenticationService.logOut(logOutRequest);
        assertFalse(logoutUserResponse.isLoggedIn());
    }

}