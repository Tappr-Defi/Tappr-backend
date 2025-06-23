package com.semicolon.africa.tapprbackend.transaction.dtos.requests;

import lombok.Getter;
import lombok.Setter;
import org.vomzersocials.user.enums.MediaType;

@Setter
@Getter
public class MediaRequest {
    private String filename;
    private String key;
    private MediaType mediaType;
}
