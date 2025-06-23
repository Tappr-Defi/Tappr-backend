package com.semicolon.africa.tapprbackend.reciepts.services.interfaces;

import org.vomzersocials.user.dtos.requests.LikeRequest;
import org.vomzersocials.user.dtos.responses.LikeResponse;

public interface LikeService {
    LikeResponse likeOrUnLike(LikeRequest likeRequest);
}
