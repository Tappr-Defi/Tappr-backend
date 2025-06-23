package com.semicolon.africa.tapprbackend.transaction.services.implementations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.data.repositories.UserRepository;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.enums.FollowType;
import org.vomzersocials.user.services.interfaces.AuthenticationService;
import org.vomzersocials.user.services.interfaces.PostService;
import org.vomzersocials.user.services.interfaces.UserService;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private AuthenticationService authenticationService;
    private final PostService postService;
    @Autowired
    private UserRepository userRepository;

    public UserServiceImpl( PostService postService) {
        this.postService = postService;
    }

    @Override
    public Mono<RegisterUserResponse> registerNewUserViaZk(ZkRegisterRequest request) {
        return authenticationService.registerWithZkLogin(request);
    }

    @Override
    public Mono<RegisterUserResponse> registerNewUserViaStandardRegistration(StandardRegisterRequest request) {
        return authenticationService.registerWithStandardLogin(request);
    }

    @Override
    public Mono<LoginResponse> loginUserViaZk(ZkLoginRequest request) {
        return authenticationService.loginWithZkLogin(request);
    }

    @Override
    public Mono<LoginResponse> loginUserViaStandard(StandardLoginRequest request) {
        return authenticationService.loginWithStandardLogin(request);
    }

    @Override
    public Mono<LogoutUserResponse> logoutUser(LogoutRequest request) {
        return authenticationService.logoutUser(request);
    }

    @Override
    public Mono<CreatePostResponse> createPost(CreatePostRequest request, String userId) {
        return postService.createPost(request, userId);
    }

    @Override
    public Mono<DeletePostResponse> deletePost(DeletePostRequest request, String userId) {
        return postService.deletePost(request, userId);
    }

    @Override
    public Mono<EditPostResponse> editPost(EditPostRequest request, String userId) {
        return postService.editPost(request, userId);
    }

    @Override
    public Mono<RepostResponse> repost(RepostRequest request, String userId) {
        return postService.repost(request, userId);
    }

    @Override
    public Mono<TokenPair> refreshTokens(String refreshToken) {
        return authenticationService.refreshTokens(refreshToken);
    }

    @Override
    public Mono<RegisterUserResponse> registerAdmin(StandardRegisterRequest request) {
        return authenticationService.registerAdmin(request);
    }

    @Override
    public int updateUserFollowCount(FollowUserRequest request, FollowType followType, boolean isIncrement) {
        User user;

        if (followType == FollowType.FOLLOWER) {
            user = userRepository.findUserById(request.getFollowingId());
            int newCount = user.getFollowerCount() + (isIncrement ? 1 : -1);
            user.setFollowerCount(Math.max(newCount, 0));
            return userRepository.save(user).getFollowerCount();
        } else if (followType == FollowType.FOLLOWING) {
            user = userRepository.findUserById(request.getFollowerId());
            int newCount = user.getFollowingCount() + (isIncrement ? 1 : -1);
            user.setFollowingCount(Math.max(newCount, 0));
            return userRepository.save(user).getFollowingCount();
        } else {
            throw new IllegalArgumentException("Unknown follow type");
        }
    }

    @Override
    public Optional<User> findById(String followingId) {
        return userRepository.findById(followingId);
    }

    @Override
    public void saveAll(List<User> users) {
        if (users != null && !users.isEmpty()) {
            userRepository.saveAll(users);
        }
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }


}
