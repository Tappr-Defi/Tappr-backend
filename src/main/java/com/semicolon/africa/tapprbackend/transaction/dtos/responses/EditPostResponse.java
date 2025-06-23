package com.semicolon.africa.tapprbackend.transaction.dtos.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.data.models.User;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class EditPostResponse {
    private String id;
    private User author;
    private String content;
    private String errorMessage;
    private LocalDateTime timestamp;
    private Boolean isEdited;
}
