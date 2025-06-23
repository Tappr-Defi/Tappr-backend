package com.semicolon.africa.tapprbackend.transaction.dtos.responses;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CreateCommentResponse {
    private String id;
    private String content;
    private String userId;
    private String postId;
    private LocalDateTime createdAt;
    private List<CreateCommentResponse> replies;
}
