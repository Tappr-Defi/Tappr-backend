package com.semicolon.africa.tapprbackend.transaction.services.interfaces;

import jakarta.validation.Valid;
import org.vomzersocials.user.data.models.User;
import org.vomzersocials.user.dtos.requests.*;
import org.vomzersocials.user.dtos.responses.*;
import org.vomzersocials.user.enums.FollowType;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface UserService {
    int updateUserFollowCount(FollowUserRequest request, FollowType followType, boolean isIncrement);
    Optional<User> findById(String followingId);
    void saveAll(List<User> users);
    List<User> findAll();
    Mono<RegisterUserResponse> registerNewUserViaZk(ZkRegisterRequest request);
    Mono<RegisterUserResponse> registerNewUserViaStandardRegistration(StandardRegisterRequest request);
    Mono<LoginResponse> loginUserViaZk(ZkLoginRequest request);
    Mono<LoginResponse> loginUserViaStandard(StandardLoginRequest request);
    Mono<LogoutUserResponse> logoutUser(LogoutRequest request);
    Mono<CreatePostResponse> createPost(CreatePostRequest request, String userId);
    Mono<DeletePostResponse> deletePost(DeletePostRequest request, String userId);
    Mono<EditPostResponse> editPost(EditPostRequest request, String userId);
    Mono<RepostResponse> repost(RepostRequest request, String userId);
    Mono<TokenPair> refreshTokens(String refreshToken);
    Mono<RegisterUserResponse> registerAdmin(@Valid StandardRegisterRequest request);
}
