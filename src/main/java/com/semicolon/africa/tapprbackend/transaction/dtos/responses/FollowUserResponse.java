package com.semicolon.africa.tapprbackend.transaction.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FollowUserResponse {
    private String message;
    private boolean followerSuccess;
    private boolean followingSuccess;
}
