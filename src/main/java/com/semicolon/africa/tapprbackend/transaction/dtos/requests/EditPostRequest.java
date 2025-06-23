package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import lombok.Data;

@Data
public class EditPostRequest {
    private String postId;
    private String content;
}
