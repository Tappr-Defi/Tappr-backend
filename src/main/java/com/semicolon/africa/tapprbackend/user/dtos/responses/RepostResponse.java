package com.semicolon.africa.tapprbackend.user.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class RepostResponse {
    private String foundUserId;
    private String repostedPostId;
    private LocalDateTime createdAt;
    private String errorMessage;
}
