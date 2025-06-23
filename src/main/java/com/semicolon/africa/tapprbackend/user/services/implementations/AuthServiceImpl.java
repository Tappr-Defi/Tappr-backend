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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
        validateSignUpDto(createNewUserRequest);
        validateUserDoesNotExist(createNewUserRequest.getEmail());
        String hashedPassword = passwordEncoder.encode(createNewUserRequest.getPassword());

        User newUser = newUserCreation(createNewUserRequest, hashedPassword);

        return getCreateNewUserResponse(newUser);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().trim().isEmpty()) {
            throw new UserNotFoundException("User with that email doesn't exist");
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

    private User checkIfUserExistsInDb(String emailLowerCase) {
        return getUserFromDb(emailLowerCase)
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

    private Optional<User> getUserFromDb(String email) {
        return userRepository.findByEmail(email);
    }

    private CreateNewUserResponse getCreateNewUserResponse(User newUser) {
        CreateNewUserResponse createNewUserResponse = new CreateNewUserResponse();
        createNewUserResponse.setMessage("User created successfully");
        createNewUserResponse.setAccessToken(jwtUtil.generateToken(newUser.getEmail(), newUser.getRole()));
        createNewUserResponse.setUserId(String.valueOf(newUser.getId()));
        return createNewUserResponse;
    }

    private User newUserCreation(CreateNewUserRequest createNewUserRequest, String hashedPassword) {
        User newUser = new User();
        newUser.setFirstName(createNewUserRequest.getFirstName());
        newUser.setLastName(createNewUserRequest.getLastName());
        newUser.setEmail(createNewUserRequest.getEmail().toLowerCase().trim());
        newUser.setKycVerified(false);
        newUser.setPasswordHash(hashedPassword);
        newUser.setPhoneNumber(createNewUserRequest.getPhoneNumber());
        newUser.setRole(Role.REGULAR);
        newUser.setCreatedAt(LocalDateTime.now());

        userRepository.save(newUser);
        return newUser;
    }

    private void validateUserDoesNotExist(String email) {
        userRepository.findByEmail(email.toLowerCase().trim())
                .ifPresent(u -> { throw new IllegalArgumentException("User already exists"); });
    }

    private void validateSignUpDto(CreateNewUserRequest signUpUserDto) {
        isRequestNull(signUpUserDto);
        isRequestEmptyOrContainWhiteSpace(signUpUserDto);
        validateNamesFormat(signUpUserDto);
        validateEmailFormat(signUpUserDto);
        validatePasswordFormat(signUpUserDto);
    }

    private static void isRequestNull(CreateNewUserRequest signUpUserDto) {
        if (signUpUserDto.getFirstName() == null ||
                signUpUserDto.getLastName() == null ||
                signUpUserDto.getEmail() == null ||
                signUpUserDto.getPassword() == null ||
                signUpUserDto.getPhoneNumber() == null
        ) {
            throw new IllegalArgumentException("Fields must not be empty, contain special character or contain whitespace");
        }
    }

    private static void isRequestEmptyOrContainWhiteSpace(CreateNewUserRequest signUpUserDto) {
        if (signUpUserDto.getFirstName().isEmpty() ||
                signUpUserDto.getLastName().isEmpty() ||
                signUpUserDto.getEmail().isEmpty() ||
                signUpUserDto.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Fields must not be empty");
        }
    }

    private static void validateNamesFormat(CreateNewUserRequest signUpUserDto) {
        if (isValidName(signUpUserDto.getFirstName())) {
            throw new IllegalArgumentException("First name contains invalid characters. Only letters, hyphens, and apostrophes are allowed.");
        }
        if (isValidName(signUpUserDto.getLastName())) {
            throw new IllegalArgumentException("Last name contains invalid characters. Only letters, hyphens, and apostrophes are allowed.");
        }
    }

    private static void validateEmailFormat(CreateNewUserRequest signUpUserDto) {
        if (!isValidEmail(signUpUserDto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static void validatePasswordFormat(CreateNewUserRequest signUpUserDto) {
        if (containsWhiteSpace(signUpUserDto.getPassword())) {
            throw new IllegalArgumentException("Password cannot contain whitespace");
        }
    }

    private static boolean containsWhiteSpace(String input) {
        return input != null && input.matches(".*\\s+.*");
    }

    private static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return true;
        }
        return !name.matches("^[a-zA-ZÀ-ÿ'.-]+$");
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[a-zA-Z0-9._%+'-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}
