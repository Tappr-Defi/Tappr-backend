package com.semicolon.africa.tapprbackend.user.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeletePostRequest {
    private String postId;
    private String userId;
}