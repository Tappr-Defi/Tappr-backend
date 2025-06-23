package com.semicolon.africa.tapprbackend.transaction.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LikeResponse {
    private String message;
    private boolean isLiked;
}
