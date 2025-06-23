package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

//@ExtendWith(MockitoExtension.class)
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

    @Test
    public void test_thatUserCanSignUp_shouldSaveUser() {
       CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        assertNotNull(response);

        User user = userRepository.findByEmail(createNewUserRequest.getEmail()).get();
        assertNotNull(user);
        assertEquals(Role.REGULAR,user.getRole());
        assertEquals(createNewUserRequest.getFirstName(),user.getFirstName());
    }

    @Test
    void testSignUpFailsWithInvalidEmail() {
        createNewUserRequest.setEmail("invalid email");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    void testSignUpFailsWithEmptyPassword() {
        createNewUserRequest.setPassword("");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    void testJwtTokenIsGenerated() {
        CreateNewUserResponse response = authenticationService.createNewUser(createNewUserRequest);
        
        assertNotNull(response.getAccessToken());
        assertFalse(response.getAccessToken().isEmpty());
    }

    @Test
    void testSignUpFailsWithSpecialCharactersInName() {
        createNewUserRequest.setFirstName("John@Daniel");
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    void testSignUpFailsWhenUserAlreadyExists() {
        // Create user first time
        authenticationService.createNewUser(createNewUserRequest);
        
        // Try to create same user again
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }

    @Test
    void testSignUpFailsWithNullFields() {
        createNewUserRequest.setFirstName(null);
        
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.createNewUser(createNewUserRequest);
        });
    }
}