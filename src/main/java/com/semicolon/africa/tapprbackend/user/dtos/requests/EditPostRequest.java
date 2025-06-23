package com.semicolon.africa.tapprbackend.user.dtos.requests;

import lombok.Data;

@Data
public class EditPostRequest {
    private String postId;
    private String content;
}
