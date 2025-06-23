package com.semicolon.africa.tapprbackend.user.services.implementations;

import com.semicolon.africa.tapprbackend.security.JwtUtil;
import com.semicolon.africa.tapprbackend.user.data.models.User;
import com.semicolon.africa.tapprbackend.user.data.repositories.UserRepository;
import com.semicolon.africa.tapprbackend.user.dtos.requests.CreateNewUserRequest;
import com.semicolon.africa.tapprbackend.user.dtos.responses.CreateNewUserResponse;
import com.semicolon.africa.tapprbackend.user.enums.Role;
import com.semicolon.africa.tapprbackend.user.services.interfaces.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
        newUser.setEmail(createNewUserRequest.getEmail());
        newUser.setKycVerified(false);
        newUser.setPasswordHash(hashedPassword);
        newUser.setPhoneNumber(createNewUserRequest.getPhoneNumber());
        newUser.setRole(Role.REGULAR);
        newUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(newUser);
        return newUser;
    }

    private void validateUserDoesNotExist(String email) {
        userRepository.findByEmail(email)
                .ifPresent(u -> { throw new IllegalArgumentException("User already exists"); });
    }

    private void validateSignUpDto(CreateNewUserRequest signUpUserDto) {
        isRequestNull(signUpUserDto);
        isRequestEmptyOrContainWhiteSpace(signUpUserDto);
        doesRequestContainSpecialCharacter(signUpUserDto);
    }

    private static void isRequestNull(CreateNewUserRequest signUpUserDto) {
        if (signUpUserDto.getFirstName() == null ||
                signUpUserDto.getLastName() == null ||
                signUpUserDto.getEmail() == null ||
                signUpUserDto.getPassword() == null) {
            throw new IllegalArgumentException("Fields must not be empty, contain special character or contain whitespace");
        }
    }

    private static void isRequestEmptyOrContainWhiteSpace(CreateNewUserRequest signUpUserDto) {
        if (signUpUserDto.getFirstName().isEmpty() || containsWhiteSpace(signUpUserDto.getFirstName()) ||
                signUpUserDto.getLastName().isEmpty() || containsWhiteSpace(signUpUserDto.getLastName()) ||
                signUpUserDto.getEmail().isEmpty() || containsWhiteSpace(signUpUserDto.getEmail()) ||
                signUpUserDto.getPassword().isEmpty() || containsWhiteSpace(signUpUserDto.getPassword())) {
            throw new IllegalArgumentException("Fields must not be empty, contain special character or contain whitespace");
        }
    }

    private static void doesRequestContainSpecialCharacter(CreateNewUserRequest signUpUserDto) {
        if (containsSpecialCharacters(signUpUserDto.getFirstName()) ||
                containsSpecialCharacters(signUpUserDto.getLastName())) {
            throw new IllegalArgumentException("Fields must not be empty, contain special character or contain whitespace");
        }
    }

    private static boolean containsWhiteSpace(String input) {
        return input != null && input.matches(".*\\s+.*");
    }

    private static boolean containsSpecialCharacters(String input) {
        return input != null && input.matches(".*[^a-zA-Z0-9].*");
    }
}
