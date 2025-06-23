package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RepostRequest {
    private String userId;
    private String postId;

}
