package com.semicolon.africa.tapprbackend.reciepts.services.interfaces;

import org.vomzersocials.user.dtos.requests.FollowUserRequest;

public interface FollowerService {
    void followUser(FollowUserRequest followUserRequest);
    void unfollowUser(FollowUserRequest followUserRequest);
    boolean isFollowing(FollowUserRequest followUserRequest);
    void toggleFollow(FollowUserRequest request);
}
