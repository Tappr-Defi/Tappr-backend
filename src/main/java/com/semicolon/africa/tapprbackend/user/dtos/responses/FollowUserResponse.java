package com.semicolon.africa.tapprbackend.user.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FollowUserResponse {
    private String message;
    private boolean followerSuccess;
    private boolean followingSuccess;
}
