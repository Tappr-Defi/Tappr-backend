package com.semicolon.africa.tapprbackend.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FollowUserRequest {
    private String followerId;
    private String followingId;
}
