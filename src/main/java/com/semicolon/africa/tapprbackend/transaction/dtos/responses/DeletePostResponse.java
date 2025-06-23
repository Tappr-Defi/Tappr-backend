package com.semicolon.africa.tapprbackend.transaction.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class DeletePostResponse {
    private String postId;
    private String message;
}
