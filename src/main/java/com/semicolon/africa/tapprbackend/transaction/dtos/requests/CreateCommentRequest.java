package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCommentRequest {
    private String postId;
    private String userId;
    private String content;
    private String parentCommentId;
}
