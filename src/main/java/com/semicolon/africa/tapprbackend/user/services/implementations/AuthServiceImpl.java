//package com.semicolon.africa.tapprbackend.user.services.implementations;
//
//import com.semicolon.africa.tapprbackend.security.JwtUtil;
//import com.semicolon.africa.tapprbackend.user.data.models.User;
//import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
//import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
//import com.semicolon.africa.tapprbackend.user.dtos.requests.LoginRequest;
//import com.semicolon.africa.tapprbackend.user.dtos.requests.LogoutRequest;
//import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
//import com.semicolon.africa.tapprbackend.user.dtos.responses.LoginResponse;
//import com.semicolon.africa.tapprbackend.user.dtos.responses.LogoutUserResponse;
//import com.semicolon.africa.tapprbackend.user.enums.Role;
//import com.semicolon.africa.tapprbackend.user.exceptions.UserNotFoundException;
//import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Slf4j
//@Service
//public class AuthServiceImpl implements AuthService {
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil;
//
//    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @Override
//    public CreateNewUserResponse createNewUser(CreateNewUserRequest createNewUserRequest) {
//        log.info("Creating a new user: " + createNewUserRequest.toString());
//        validateUserDoesNotExist(createNewUserRequest);
//        log.info("Validation successful");
//        String hashedPassword = passwordEncoder.encode(createNewUserRequest.getPassword());
//        log.info("Password has been hashed");
//        User newUser = newUserCreation(createNewUserRequest, hashedPassword);
//        return getCreateNewUserResponse(newUser);
//    }
//
//    @Override
//    public LoginResponse login(LoginRequest loginRequest) {
//        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
//            throw new UserNotFoundException("Email is required");
//        }
//
//        String emailLowerCase = loginRequest.getEmail().toLowerCase().trim();
//        User user = checkIfUserExistsInDb(emailLowerCase);
//        boolean isValidCredentials = passwordEncoder.matches(loginRequest.getPassword(),
//                user.getPasswordHash());
//        if (!isValidCredentials) throw new IllegalArgumentException("Incorrect credentials");
//
//        user.setLoggedIn(true);
//        userRepository.save(user);
//
//        return getLoginResponse(user);
//    }
//
//    private User checkIfUserExistsInDb(String email) {
//        return getUserFromDb(email)
//                .orElseThrow(() -> new UserNotFoundException("User with that email doesn't exist"));
//    }
//
//    @Override
//    public LogoutUserResponse logOut(LogoutRequest logOutRequest) {
//        String emailLowerCase = logOutRequest.getEmail().toLowerCase().trim();
//        User user = getUserFromDb(emailLowerCase)
//                .orElseThrow(() -> new IllegalArgumentException("User with that email doesn't exist"));
//        if (user.isLoggedIn()) {
//            user.setLoggedIn(false);
//            userRepository.save(user);
//            return getLogoutUserResponse(user);
//        } else {
//            throw new IllegalArgumentException("You're already logged out");
//        }
//    }
//
//    private Optional<User> getUserFromDb(String email) {
//        return userRepository.findByEmail(email);
//    }
//
//    private static LogoutUserResponse getLogoutUserResponse(User user) {
//        LogoutUserResponse response = new LogoutUserResponse();
//        response.setLoggedIn(user.isLoggedIn());
//        response.setMessage("Logged Out Successfully");
//        return response;
//    }
//
//
//    private LoginResponse getLoginResponse(User user) {
//        LoginResponse loginResponse = new LoginResponse();
//        loginResponse.setMessage("Logged in successfully");
//        loginResponse.setLoggedIn(true);
//        loginResponse.setRefreshToken("");
//        loginResponse.setAccessToken(jwtUtil.generateToken(user.getEmail(), user.getRole()));
//        loginResponse.setUserId(String.valueOf(user.getId()));
//        loginResponse.setRole(user.getRole());
//        return loginResponse;
//    }
//
//    private CreateNewUserResponse getCreateNewUserResponse(User newUser) {
//        CreateNewUserResponse createNewUserResponse = new CreateNewUserResponse();
//        createNewUserResponse.setMessage("User created successfully");
//        createNewUserResponse.setPhoneNumber(newUser.getPhoneNumber());
//        createNewUserResponse.setUserId(String.valueOf(newUser.getId()));
//        createNewUserResponse.setEmail(newUser.getEmail());
//        log.info("User saved to database");
//        return createNewUserResponse;
//    }
//
//    private User newUserCreation(CreateNewUserRequest createNewUserRequest, String hashedPassword) {
//        User newUser = new User();
//        newUser.setFirstName(createNewUserRequest.getFirstName());
//        newUser.setLastName(createNewUserRequest.getLastName());
//        newUser.setEmail(createNewUserRequest.getEmail().toLowerCase().trim());
//        newUser.setKycVerified(false);
//        newUser.setPasswordHash(hashedPassword);
//        newUser.setPhoneNumber(createNewUserRequest.getPhoneNumber().trim());
//        newUser.setRole(Role.REGULAR);
//        newUser.setCreatedAt(LocalDateTime.now());
//        log.info("Saving user to database");
//        userRepository.save(newUser);
//        return newUser;
//    }
//
//    private void validateUserDoesNotExist(CreateNewUserRequest request) {
//        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
//            throw new IllegalArgumentException("User with this email already exists");
//        }
//        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
//            throw new IllegalArgumentException("Phone number already registered");
//        }
//    }
//
//
//}

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public CreateNewUserResponse createNewUser(CreateNewUserRequest createNewUserRequest) {
        log.info("Creating a new user: " + createNewUserRequest.toString());
        validateSignUpRequest(createNewUserRequest);
        validateUserDoesNotExist(createNewUserRequest);
        log.info("Validation successful");
        String hashedPassword = passwordEncoder.encode(createNewUserRequest.getPassword());
        log.info("Password has been hashed");
        User newUser = newUserCreation(createNewUserRequest, hashedPassword);
        return getCreateNewUserResponse(newUser);
    }

    private void validateSignUpRequest(CreateNewUserRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Email format validation
        String emailRegex = "^[\\w+.'-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!request.getEmail().matches(emailRegex)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Name validation - only letters, apostrophes, and hyphens allowed
        String nameRegex = "^[\\p{L}' -]+$";
        if (!request.getFirstName().matches(nameRegex)) {
            throw new IllegalArgumentException("First name contains invalid characters");
        }
        if (!request.getLastName().matches(nameRegex)) {
            throw new IllegalArgumentException("Last name contains invalid characters");
        }

        // Password whitespace check
        if (request.getPassword().contains(" ")) {
            throw new IllegalArgumentException("Password must not contain whitespace");
        }
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            throw new UserNotFoundException("Email is required");
        }

        String emailLowerCase = loginRequest.getEmail().toLowerCase().trim();
        User user = checkIfUserExistsInDb(emailLowerCase);
        boolean isValidCredentials = passwordEncoder.matches(loginRequest.getPassword(),
                user.getPasswordHash());
        if (!isValidCredentials) throw new IllegalArgumentException("Incorrect credentials");

        user.setLoggedIn(true);
        userRepository.save(user);

        return getLoginResponse(user);
    }

    private User checkIfUserExistsInDb(String email) {
        return getUserFromDb(email)
                .orElseThrow(() -> new UserNotFoundException("User with that email doesn't exist"));
    }

    @Override
    public LogoutUserResponse logOut(LogoutRequest logOutRequest) {
        String emailLowerCase = logOutRequest.getEmail().toLowerCase().trim();
        User user = getUserFromDb(emailLowerCase)
                .orElseThrow(() -> new IllegalArgumentException("User with that email doesn't exist"));
        if (user.isLoggedIn()) {
            user.setLoggedIn(false);
            userRepository.save(user);
            return getLogoutUserResponse(user);
        } else {
            throw new IllegalArgumentException("You're already logged out");
        }
    }

    private Optional<User> getUserFromDb(String email) {
        return userRepository.findByEmail(email);
    }

    private static LogoutUserResponse getLogoutUserResponse(User user) {
        LogoutUserResponse response = new LogoutUserResponse();
        response.setLoggedIn(user.isLoggedIn());
        response.setMessage("Logged Out Successfully");
        return response;
    }


    private LoginResponse getLoginResponse(User user) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setMessage("Logged in successfully");
        loginResponse.setLoggedIn(true);
        loginResponse.setRefreshToken("");
        loginResponse.setAccessToken(jwtUtil.generateToken(user.getEmail(), user.getRole()));
        loginResponse.setUserId(String.valueOf(user.getId()));
        loginResponse.setRole(user.getRole());
        return loginResponse;
    }

    private CreateNewUserResponse getCreateNewUserResponse(User newUser) {
        CreateNewUserResponse createNewUserResponse = new CreateNewUserResponse();
        createNewUserResponse.setMessage("User created successfully");
        createNewUserResponse.setPhoneNumber(newUser.getPhoneNumber());
        createNewUserResponse.setUserId(String.valueOf(newUser.getId()));
        createNewUserResponse.setEmail(newUser.getEmail());
        log.info("User saved to database");
        return createNewUserResponse;
    }

    private User newUserCreation(CreateNewUserRequest request, String hashedPassword) {
        User newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail().toLowerCase().trim());
        newUser.setKycVerified(false);
        newUser.setPasswordHash(hashedPassword);
        newUser.setPhoneNumber(request.getPhoneNumber().trim());
        newUser.setRole(Role.REGULAR);
        newUser.setCreatedAt(LocalDateTime.now());
        log.info("User object before saving: {}", newUser);
        User savedUser = userRepository.save(newUser);
        log.info("User saved with ID: {}", savedUser.getId());
        return savedUser;
    }

    private void validateUserDoesNotExist(CreateNewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new IllegalArgumentException("User with this email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered");
        }
    }
}