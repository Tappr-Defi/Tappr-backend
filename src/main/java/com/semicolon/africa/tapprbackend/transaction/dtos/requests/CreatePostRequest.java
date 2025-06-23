package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.data.models.User;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class CreatePostRequest {
    private User author;
    private String content;
    private List<UUID> mediaIds;

    @Setter
    @Getter
    public static class Author {
        private String userName;
    }
}
